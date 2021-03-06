/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.ui

import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection

/**
 * The receiver scope of a layout's measure lambda. The return value of the
 * measure lambda is [MeasureResult], which should be returned by [layout]
 */
abstract class MeasureScope : IntrinsicMeasureScope() {
    /**
     * Interface holding the size and alignment lines of the measured layout, as well as the
     * children positioning logic.
     * [placeChildren] is the function used for positioning children. [Placeable.place] should
     * be called on children inside [placeChildren].
     * The alignment lines can be used by the parent layouts to decide layout, and can be queried
     * using the [Placeable.get] operator. Note that alignment lines will be inherited by parent
     * layouts, such that indirect parents will be able to query them as well.
     */
    interface MeasureResult {
        val width: Int
        val height: Int
        val alignmentLines: Map<AlignmentLine, Int>
        fun placeChildren()
    }

    /**
     * Sets the size and alignment lines of the measured layout, as well as
     * the positioning block that defines the children positioning logic.
     * The [placementBlock] is a lambda used for positioning children. [Placeable.place] should
     * be called on children inside placementBlock.
     * The [alignmentLines] can be used by the parent layouts to decide layout, and can be queried
     * using the [Placeable.get] operator. Note that alignment lines will be inherited by parent
     * layouts, such that indirect parents will be able to query them as well.
     *
     * @param width the measured width of the layout
     * @param height the measured height of the layout
     * @param alignmentLines the alignment lines defined by the layout
     * @param placementBlock block defining the children positioning of the current layout
     */
    /*inline*/ fun layout(
        width: Int,
        height: Int,
        alignmentLines: Map<AlignmentLine, Int> = emptyMap(),
        /*crossinline*/
        placementBlock: Placeable.PlacementScope.() -> Unit
    ) = object : MeasureResult {
        override val width = width
        override val height = height
        override val alignmentLines = alignmentLines
        override fun placeChildren() {
            with(InnerPlacementScope) {
                val previousParentWidth = parentWidth
                val previousLayoutDirection = parentLayoutDirection
                updateValuesForRtlMirroring(layoutDirection, width)
                placementBlock()
                updateValuesForRtlMirroring(previousLayoutDirection, previousParentWidth)
            }
        }
    }

    internal companion object {
        object InnerPlacementScope : Placeable.PlacementScope() {
            override var parentLayoutDirection = LayoutDirection.Ltr
                private set
            override var parentWidth = 0
                private set

            fun updateValuesForRtlMirroring(
                parentLayoutDirection: LayoutDirection,
                parentWidth: Int
            ) {
                this.parentLayoutDirection = parentLayoutDirection
                this.parentWidth = parentWidth
            }
        }
    }
}

/**
 * A function for performing layout measurement.
 */
typealias MeasureBlock = MeasureScope.(List<Measurable>, Constraints) -> MeasureScope.MeasureResult