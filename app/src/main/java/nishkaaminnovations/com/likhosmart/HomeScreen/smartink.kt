package nishkaaminnovations.com.likhosmart.HomeScreen

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.ir
import nishkaaminnovations.com.likhosmart.databinding.FragmentSmartinkBinding
import java.util.Locale
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSortedSet
import nishkaaminnovations.com.likhosmart.R

class smartink : Fragment() {

    private lateinit var binding: FragmentSmartinkBinding
    private lateinit var ink: ir
    private var dataList: List<String> = emptyList()
    private lateinit var languageAdapter: ArrayAdapter<ModelLanguageContainer>
    private var model:String=""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSmartinkBinding.inflate(inflater, container, false)
        ink = ir(requireContext())
        languageAdapter = populateLanguageAdapter()
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.modelSpinner.adapter = languageAdapter
       binding.modelSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    val languageCode =
                        (parent.adapter.getItem(position) as ModelLanguageContainer).languageTag ?: return
                    Log.i(TAG, "Selected language: $languageCode")
                    model=languageCode
                    downloadSelectedModel(languageCode)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    Log.i(TAG, "No language selected")
                }

            }
        binding.backtodocspagebuttonsmartink.setOnClickListener{
            findNavController().navigate(R.id.action_smartink_to_documentsFragment)
        }
        return binding.root
    }
    /*
    Method to download the selected model
     */
    private fun downloadSelectedModel(model:String)
    {
        binding.deletemodel.visibility=View.GONE
        // Trigger the model download and initialization
        ink.downloadAndInitializeModel(model)
        toggleState(false)

        // Coroutine to check download status
        CoroutineScope(Dispatchers.IO).launch {
            ink.isDownloadSuccessful.collect { status ->
                when (status) {
                    "null" -> { }
                    "true" -> {
                        withContext(Dispatchers.Main) {
                            binding.deletemodel.visibility = View.VISIBLE
                            binding.downladModel.visibility = View.GONE
                            toggleState(true)
                            showSnackbar("Model Downloaded")
                            ink._isDownloadSuccessful.value = "null"

                        }
                    }
                    "false" -> {
                        withContext(Dispatchers.Main) {
                            showSnackbar("Model download failed")
                            binding.deletemodel.visibility = View.INVISIBLE
                            binding.downladModel.visibility = View.GONE
                            toggleState(true)
                            ink._isDownloadSuccessful.value = "null"
                        }
                    }
                    "already" -> {
                        withContext(Dispatchers.Main) {
                            showSnackbar("Model already downloaded")
                            binding.deletemodel.visibility = View.VISIBLE
                            binding.downladModel.visibility = View.GONE
                            toggleState(true)
                            ink._isDownloadSuccessful.value = "null"
                        }
                    }
                }
            }
        }

    }
    private fun toggleState(enable: Boolean) {
        binding.modelSpinner.isEnabled = enable
        binding.deletemodel.isEnabled = enable
        binding.deletemodel.alpha = if (enable) 1.0f else 0.5f
        binding.downladModel.isEnabled = enable
        binding.downladModel.alpha = if (enable) 1.0f else 0.5f
        binding.loadinglayout.visibility = if (enable) View.INVISIBLE else View.VISIBLE
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }


private class ModelLanguageContainer
private constructor(private val label: String, val languageTag: String?) :
    Comparable<ModelLanguageContainer> {

    var downloaded: Boolean = false

    override fun toString(): String {
        return when {
            languageTag == null -> label
            downloaded -> "   [D] $label"
            else -> "   $label"
        }
    }

    override fun compareTo(other: ModelLanguageContainer): Int {
        return label.compareTo(other.label)
    }

    companion object {
        /** Populates and returns a real model identifier, with label and language tag. */
        fun createModelContainer(label: String, languageTag: String?): ModelLanguageContainer {
            // Offset the actual language labels for better readability
            return ModelLanguageContainer(label, languageTag)
        }

        /** Populates and returns a label only, without a language tag. */
        fun createLabelOnly(label: String): ModelLanguageContainer {
            return ModelLanguageContainer(label, null)
        }
    }
}

private fun populateLanguageAdapter(): ArrayAdapter<ModelLanguageContainer> {
    val languageAdapter =
        ArrayAdapter<ModelLanguageContainer>(requireContext(), android.R.layout.simple_spinner_item)
    languageAdapter.add(ModelLanguageContainer.createLabelOnly("Select language"))
    languageAdapter.add(ModelLanguageContainer.createLabelOnly("Non-text Models"))

    // Manually add non-text models first
    for (languageTag in NON_TEXT_MODELS.keys) {
        languageAdapter.add(
            ModelLanguageContainer.createModelContainer(NON_TEXT_MODELS[languageTag]!!, languageTag)
        )
    }
    languageAdapter.add(ModelLanguageContainer.createLabelOnly("Text Models"))
    val textModels = ImmutableSortedSet.naturalOrder<ModelLanguageContainer>()
    for (modelIdentifier in DigitalInkRecognitionModelIdentifier.allModelIdentifiers()) {
        if (NON_TEXT_MODELS.containsKey(modelIdentifier.languageTag)) {
            continue
        }
        if (modelIdentifier.languageTag.endsWith(Companion.GESTURE_EXTENSION)) {
            continue
        }
        textModels.add(buildModelContainer(modelIdentifier, "Script"))
    }
    languageAdapter.addAll(textModels.build())
    languageAdapter.add(ModelLanguageContainer.createLabelOnly("Gesture Models"))
    val gestureModels = ImmutableSortedSet.naturalOrder<ModelLanguageContainer>()
    for (modelIdentifier in DigitalInkRecognitionModelIdentifier.allModelIdentifiers()) {
        if (!modelIdentifier.languageTag.endsWith(Companion.GESTURE_EXTENSION)) {
            continue
        }
        gestureModels.add(buildModelContainer(modelIdentifier, "Script gesture classifier"))
    }
    languageAdapter.addAll(gestureModels.build())
    return languageAdapter
}

private fun buildModelContainer(
    modelIdentifier: DigitalInkRecognitionModelIdentifier,
    labelSuffix: String
): ModelLanguageContainer {
    val label = StringBuilder()
    label.append(Locale(modelIdentifier.languageSubtag).displayName)
    if (modelIdentifier.regionSubtag != null) {
        label.append(" (").append(modelIdentifier.regionSubtag).append(")")
    }
    if (modelIdentifier.scriptSubtag != null) {
        label.append(", ").append(modelIdentifier.scriptSubtag).append(" ").append(labelSuffix)
    }
    return ModelLanguageContainer.createModelContainer(
        label.toString(),
        modelIdentifier.languageTag
    )
}

companion object {
    private const val TAG = "MLKDI.Activity"
    private val NON_TEXT_MODELS =
        ImmutableMap.of(
            "zxx-Zsym-x-autodraw",
            "Autodraw",
            "zxx-Zsye-x-emoji",
            "Emoji",
            "zxx-Zsym-x-shapes",
            "Shapes"
        )
    private const val GESTURE_EXTENSION = "-x-gesture"
}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val window = requireActivity().window
        WindowCompat.setDecorFitsSystemWindows(window, false) // Reverts to default behavior

    }
}
