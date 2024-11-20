package nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews

import android.graphics.Path
import android.graphics.RectF
import android.util.Log
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock


class QuadTree internal constructor(private val boundary: RectF) {
    private val MAX_CAPACITY = 4
    private var pathList: MutableList<BoundingPath>
    private var divided = false
    private var northeast: QuadTree? = null
    private var northwest: QuadTree? = null
    private var southeast: QuadTree? = null
    private var southwest: QuadTree? = null

    init {
        pathList = ArrayList()
    }

    fun insert(path: BoundingPath): Boolean {
        Log.d("insert", "insert: step1")

        if (!RectF.intersects(boundary, path.bounds)) {
            Log.d(
                "insert",
                "insert: Path bounds " + path.bounds.toString() + " do not intersect with boundary " + boundary.toString()
            )
            return false
        }

        if (pathList.size < MAX_CAPACITY && !divided) {
            Log.d("insert", "insert: Path added to pathList with bounds: " + path.bounds.toString())
            return pathList.add(path)
        }

        if (!divided) {
            Log.d("insert", "insert: step4 - Subdividing")
            subdivide()
        }

        Log.d("insert", "insert: step5 - Attempting to insert into quadrants")
        val inserted =
            northeast!!.insert(path) || northwest!!.insert(path) || southeast!!.insert(path) || southwest!!.insert(
                path
            )

        if (!inserted) {
            Log.d(
                "insert",
                "insert: Path could not be inserted into any child, keeping in current node"
            )
        }

        return inserted || pathList.add(path)
    }


    private fun subdivide() {
        val midX = boundary.left + (boundary.width() / 2)
        val midY = boundary.top + (boundary.height() / 2)

        Log.d("subdivide", "Boundary: $boundary")
        Log.d("subdivide", "Midpoint: ($midX, $midY)")

        northeast = QuadTree(RectF(midX, boundary.top, boundary.right, midY))
        northwest = QuadTree(RectF(boundary.left, boundary.top, midX, midY))
        southeast = QuadTree(RectF(midX, midY, boundary.right, boundary.bottom))
        southwest = QuadTree(RectF(boundary.left, midY, midX, boundary.bottom))

        divided = true

        val remainingPaths: MutableList<BoundingPath> = ArrayList()
        for (p in ArrayList<BoundingPath>(pathList)) {
            Log.d("subdivide", "Checking path bounds: " + p.bounds.toString())
            val inserted =
                northeast!!.insert(p) || northwest!!.insert(p) || southeast!!.insert(p) || southwest!!.insert(
                    p
                )
            if (!inserted) {
                Log.d("subdivide", "Path could not be inserted into any quadrant.")
                remainingPaths.add(p)
            }
        }

        pathList = remainingPaths
    }

    //    public List<Path> search(RectF lassoToolBounds) {
    //        List<Path> intersecting = new ArrayList<>();
    //
    //        // Return an empty list if no intersection occurs
    //        if (!RectF.intersects(boundary, lassoToolBounds)) {
    //            return intersecting;
    //        }
    //
    //        // Add paths that intersect with the lasso tool's bounds
    //        for (BoundingPath path : pathList) {
    //            Log.d("search", "search: patlist size = "+pathList.size());
    //            if (RectF.intersects(path.bounds, lassoToolBounds)) {
    //                intersecting.add(path.path);
    //            }
    //        }
    //
    //        // Recursively search in the subdivided quadrants and collect results
    //        if (divided) {
    //            intersecting.addAll(northeast.search(lassoToolBounds));
    //            intersecting.addAll(northwest.search(lassoToolBounds));
    //            intersecting.addAll(southeast.search(lassoToolBounds));
    //            intersecting.addAll(southwest.search(lassoToolBounds));
    //        }
    //
    //        Log.d("search", "search: list size " + intersecting.size());
    //        return intersecting;
    //    }
    //
    //
    fun search(lassoToolBounds: RectF?): List<Path> {
        val intersecting: MutableList<Path> = ArrayList()
        val lock = ReentrantLock()

        if (!RectF.intersects(boundary, lassoToolBounds!!)) {
            return intersecting // Return empty list if no intersection occurs
        }

        // Add paths that intersect with the lasso tool's bounds
        for (path in pathList) {
            if (RectF.intersects(path.bounds, lassoToolBounds)) {
                intersecting.add(path.path)
            }
        }

        // If the node is subdivided, search in the child quadrants concurrently
        if (divided) {
            val executor = Executors.newFixedThreadPool(4)

            // Callable tasks for each quadrant
            val northeastTask =
                Callable {
                    northeast!!.search(
                        lassoToolBounds
                    )
                }
            val northwestTask =
                Callable {
                    northwest!!.search(
                        lassoToolBounds
                    )
                }
            val southeastTask =
                Callable {
                    southeast!!.search(
                        lassoToolBounds
                    )
                }

            val southwestTask =
                Callable {
                    southwest!!.search(
                        lassoToolBounds
                    )
                }

            try {
                // Submit tasks and gather results
                val neFuture = executor.submit(northeastTask)
                val nwFuture = executor.submit(northwestTask)
                val seFuture = executor.submit(southeastTask)
                val swFuture = executor.submit(southwestTask)

                // Safely add all results to the intersecting list
                lock.lock()
                try {
                    intersecting.addAll(neFuture.get())
                    intersecting.addAll(nwFuture.get())
                    intersecting.addAll(seFuture.get())
                    intersecting.addAll(swFuture.get())
                } finally {
                    lock.unlock()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                executor.shutdown() // Make sure to shut down the executor
            }
        }

        Log.d("search", "search: list size " + intersecting.size)
        return intersecting
    }

    class BoundingPath internal constructor(var path: Path) {
        var bounds: RectF = RectF()

        init {
            path.computeBounds(bounds, true)
        }
    }
}