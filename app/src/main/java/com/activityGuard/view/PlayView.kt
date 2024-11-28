package com.activityGuard.view

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.activityGuard.confuseapp.R


/**
 * Created by DengLongFei
 * 2021/6/28
 * 播放动画view
 */
class PlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint: Paint = Paint()

    //偏移
    private var offset = 0f

    //矩形宽度
    private val mWidth by lazy { measuredWidth / 7.toFloat() }

    //默认矩形高度
    private val mHeight by lazy { measuredHeight / 4 }

    //半径
    private val radius by lazy { mWidth / 2 }


    val animator: ValueAnimator by lazy {     //动画
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 500
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            addUpdateListener {
                val value = it.animatedValue as Float
                offset = value * (measuredHeight / 4)
                invalidate()
            }
        }
    }

    init {
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        attrs?.also {
            val typedArray: TypedArray =
                context.obtainStyledAttributes(attrs, R.styleable.PlayView)
            val color =
                typedArray.getColor(R.styleable.PlayView_color, Color.parseColor("#17C4FF"))
            typedArray.recycle()
            paint.color = color
        }
    }

    /**
     * 设置颜色
     */
    fun setColor(color: String) {
        paint.color = color.parseColor()
    }


    val rectList by lazy {
        listOf(RectF(), RectF(), RectF())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //画圆角矩形1
        canvas.drawRoundRect(
            rectList[0].apply {
                set(
                    mWidth * 1,
                    0 + offset,
                    mWidth * 2,
                    measuredHeight - offset,
                )
            }, radius,
            radius,
            paint
        )
        //画圆角矩形2
        canvas.drawRoundRect(
            rectList[1].apply {
                set(
                    mWidth * 3,
                    mHeight - offset,
                    mWidth * 4,
                    (measuredHeight - mHeight) + offset
                )
            },
            radius,
            radius,
            paint
        )
        //画圆角矩形3
        canvas.drawRoundRect(
            rectList[2].apply {
                set(
                    mWidth * 5,
                    0 + offset,
                    mWidth * 6,
                    measuredHeight - offset
                )
            },
            radius,
            radius,
            paint
        )
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.cancel()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animator.start()
    }

    /**
     * string转颜色
     */
    fun String.parseColor(): Int {
        return try {
            Color.parseColor(this)
        } catch (e: Exception) {
            Color.parseColor("#FFECF4")
        }
    }
}