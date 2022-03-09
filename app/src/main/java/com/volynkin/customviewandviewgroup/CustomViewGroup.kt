package com.volynkin.customviewandviewgroup

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.withStyledAttributes
import androidx.core.view.marginTop
import java.lang.Integer.max

class CustomViewGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private val firstChild: View?
        get() = if (childCount > 0) getChildAt(0) else null
    private val secondChild: View?
        get() = if (childCount > 1) getChildAt(1) else null

    private var verticalOffset = 0

    init {
        if (attrs != null) {
            context.withStyledAttributes(attrs, R.styleable.CustomViewGroup, defStyleAttr) {
                verticalOffset =
                    getDimensionPixelOffset(R.styleable.CustomViewGroup_cvg_verticalOffset, 0)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        checkChildCount()

        firstChild?.let { measureChild(it, widthMeasureSpec) }
        secondChild?.let { measureChild(it, widthMeasureSpec) }

        val firstChildWidth = firstChild?.measuredWidth ?: 0
        val firstChildHeight = firstChild?.measuredHeight ?: 0
        val secondChildWidth = secondChild?.measuredWidth ?: 0
        val secondChildHeight = secondChild?.measuredHeight ?: 0

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec) - paddingStart - paddingEnd

        val isChildrenOnSameLine =
            firstChildWidth + secondChildWidth < widthSize || widthMode == MeasureSpec.UNSPECIFIED
        val width = when (widthMode) {
            MeasureSpec.AT_MOST -> firstChildWidth + secondChildWidth
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.UNSPECIFIED -> {
                if (isChildrenOnSameLine) {
                    firstChildWidth + secondChildWidth
                } else {
                    max(firstChildWidth, secondChildWidth)
                }
            }
            else -> error("Unreachable")
        }
        val height = if (isChildrenOnSameLine) {
            max(firstChildHeight, secondChildHeight)
        } else {
            firstChildHeight + secondChildHeight + verticalOffset
        }
        setMeasuredDimension(width + paddingStart + paddingEnd, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        firstChild?.layout(
            paddingLeft,
            paddingTop,
            paddingLeft + (firstChild?.measuredWidth ?: 0),
            paddingTop + (firstChild?.measuredHeight ?: 0)
        )
        secondChild?.layout(
            r - l - paddingStart - (secondChild?.measuredWidth ?: 0),
            b - t - paddingBottom - (secondChild?.measuredHeight ?: 0),
            r - l - paddingStart,
            b - t - paddingBottom
        )
    }

    private fun measureChild(child: View, widthMeasureSpec: Int) {
        val specSize = MeasureSpec.getSize(widthMeasureSpec) - paddingStart - paddingEnd
        val childWidthSpec = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.AT_MOST -> widthMeasureSpec
            MeasureSpec.EXACTLY -> MeasureSpec.makeMeasureSpec(specSize, MeasureSpec.AT_MOST)
            MeasureSpec.UNSPECIFIED -> widthMeasureSpec
            else -> error("Unreachable")
        }
        val childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        child.measure(childWidthSpec, childHeightSpec)
    }

    private fun checkChildCount() {
        if (childCount > 2) error("CustomViewGroup should not contain more then 2 children")
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams {
        return MarginLayoutParams(p)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    override fun checkLayoutParams(p: LayoutParams?): Boolean {
        return p is MarginLayoutParams
    }
}