package nishkaaminnovations.com.likhosmart.WorkShop

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import nishkaaminnovations.com.likhosmart.R
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.CustomLayout
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.onChildViewClickListener
import nishkaaminnovations.com.likhosmart.databinding.FragmentWorkShopBinding

/*
This class will work as the workshop for the document editing.
 */
class WorkShop : Fragment(){

    /*
    The binding variable for thi class.
     */
    private lateinit var binding: FragmentWorkShopBinding
    /*
    Array to hold the Main buttons.
     */
    private lateinit var mainButtonArray: Array<AppCompatImageButton>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=FragmentWorkShopBinding.inflate(inflater,container,false)
        /*
        Adding the main buttons to an array.
         */
        mainButtonArray= arrayOf(binding.textButton,binding.drawButton,binding.audioButton,binding.imageButton,binding.shapeButton,binding.lassoButton,binding.lockButton)
        /*
        initialising the buttons.
         */
        initializeMainButton()
        initialiseTextEditingButtons()
        return binding.root
    }

    /*
    Method to change the colors of the buttons
     */
    private fun changeState(button: AppCompatImageButton){
        for (i in mainButtonArray){
            if(i==button){
                button.isSelected=true
            }
            else{
                i.isSelected=false
            }
        }

    }
    /*
    Method to initialize the Main buttons
     */
    private fun initializeMainButton(){
        /*
        initializing text button.
         */
        binding.textButton.setOnClickListener{
            changeState(binding.textButton)
        }
        /*
        initializing draw button button.
         */
        binding.drawButton.setOnClickListener{
            changeState(binding.drawButton)
        }
        /*
        initializing shape button.
         */
        binding.shapeButton.setOnClickListener{
            changeState(binding.shapeButton)
        }
        /*
        initializing audio button.
         */
        binding.audioButton.setOnClickListener{
            changeState(binding.audioButton)
        }
        /*
        initializing image button.
         */
        binding.imageButton.setOnClickListener{
            changeState(binding.imageButton)
        }
        /*
        initializing text button.
         */
        binding.lassoButton.setOnClickListener{
            changeState(binding.lassoButton)
        }
        /*
        initializing lock button.
         */
        binding.lockButton.setOnClickListener{
            changeState(binding.lockButton)
        }
    }

    /*
    Method to initialize the text editing button.
     */
    private fun initialiseTextEditingButtons(){
        /*
        initialising font type button.
         */
        binding.fontButton.setOnClickListener{

            modifySelection(it as AppCompatImageButton)
        }
        /*
         initialising color type button.
         */
        binding.colorButton.setOnClickListener{
            modifySelection(it as AppCompatImageButton)
        }
        /*
         initialising font size button.
         */
        binding.fontSizeButton.setOnClickListener{
            modifySelection(it as AppCompatImageButton)
        }
        /*
         initialising bold button.
         */
        binding.boldButton.setOnClickListener{
            modifySelection(it as AppCompatImageButton)
        }
        /*
         initialising italic button.
         */
        binding.italicButton.setOnClickListener{
            modifySelection(it as AppCompatImageButton)
        }
        /*
         initialising underline button.
         */
        binding.underlineButton.setOnClickListener{
            modifySelection(it as AppCompatImageButton)
        }
        /*
         initialising strike through button.
         */
        binding.strikeThroughButton.setOnClickListener{
            modifySelection(it as AppCompatImageButton)
        }
        /*
         initialising hyper link button.
         */
        binding.hyperlinkButton.setOnClickListener{
            modifySelection(it as AppCompatImageButton)
        }
        /*
         initialising alignment button.
         */
        binding.alingmentButton.setOnClickListener{
            modifySelection(it as AppCompatImageButton)
        }
    }
    /*
    Method to modify the selection of buttons in text editing layout.
     */
    private  fun modifySelection(button:AppCompatImageButton){
        if(button.isSelected){
            button.isSelected=false
        }
        else{
            button.isSelected=true
        }
    }

}