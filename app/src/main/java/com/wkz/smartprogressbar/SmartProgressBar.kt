package com.wkz.smartprogressbar

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

/**
 * 自定义的进度条
 * 样式风格有水平、竖直、圆环、扇形、......
 *
 * @author wkz
 * @date 2019/05/05 21:23
 */
class SmartProgressBar @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr),
        ValueAnimator.AnimatorUpdateListener {
    @Target(AnnotationTarget.FIELD)
    @kotlin.annotation.Retention(AnnotationRetention.BINARY)
    annotation class ShapeStyle {
        companion object {
            /**
             * 水平样式
             */
            var HORIZONTAL = 0

            /**
             * 竖直样式
             */
            var VERTICAL = 1

            /**
             * 圆环样式
             */
            var RING = 2

            /**
             * 扇形样式
             */
            var SECTOR = 3
        }
    }

    /**
     * 进度条背景颜色
     */
    private var mProgressBarBgColor = DEFAULT_PROGRESS_BAR_BG_COLOR

    /**
     * 进度条背景渐变
     */
    private var mProgressBarBgGradient = false

    /**
     * 进度条背景透明度
     */
    private var mProgressBarBgAlpha = 0f

    /**
     * 进度颜色
     */
    private var mProgressStartColor = DEFAULT_PROGRESS_COLOR
    private var mProgressCenterColor = DEFAULT_PROGRESS_COLOR
    private var mProgressEndColor = DEFAULT_PROGRESS_COLOR
    private var mProgressColorsResId = 0
    private var mProgressPositionsResId = 0
    private var mProgressColors: IntArray? = null
    private var mProgressPositions: FloatArray? = null

    /**
     * 边框颜色
     */
    private var mBorderColor = DEFAULT_BORDER_COLOR

    /**
     * 边框宽度
     */
    private var mBorderWidth = 0f

    /**
     * 进度提示文字大小
     */
    private var mPercentTextSize =
            DEFAULT_PERCENT_TEXT_SIZE

    /**
     * 进度提示文字颜色
     */
    private var mPercentTextColor = DEFAULT_PERCENT_TEXT_COLOR

    /**
     * 进度条中心X坐标
     */
    private var mCenterX = 0f

    /**
     * 进度条中心Y坐标
     */
    private var mCenterY = 0f

    /**
     * 进度条样式
     */
    private var mShapeStyle = ShapeStyle.HORIZONTAL

    /**
     * 水平、竖直进度条圆角半径；圆环/扇形内圆半径
     */
    private var mRadius = 0f

    /**
     * 圆环/扇形是否顺时针方向绘制
     */
    private var mClockwise = true

    /**
     * 水平、竖直进度条圆角半径
     */
    private var mTopLeftRadius = 0f
    private var mTopRightRadius = 0f
    private var mBottomLeftRadius = 0f
    private var mBottomRightRadius = 0f
    private lateinit var mRadii: FloatArray

    /**
     * 进度最大值
     */
    private var max = DEFAULT_MAX

    /**
     * 进度值
     */
    private var progress = 0f

    /**
     * 进度文字是否显示百分号
     */
    private var mIsShowPercentSign = false

    /**
     * 进度文字是否显示
     */
    private var mIsShowPercentText = false

    /**
     * 进度画笔
     */
    private var mProgressPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mStartProgressPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mEndProgressPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 进度条背景画笔
     */
    private var mProgressBarBgPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 进度百分比字体画笔
     */
    private var mPercentTextPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 边框画笔
     */
    private var mBorderPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 绘制路径
     */
    private val mProgressPath = Path()
    private val mStartProgressPath = Path()
    private val mEndProgressPath = Path()
    private val mBorderPath = Path()
    private val mProgressBarBgPath = Path()
    private val mShadowPath = Path()

    /**
     * 是否执行动画
     */
    private var mIsAnimated = true
    private var mAnimator: ValueAnimator? = null
    private var mDuration = DEFAULT_ANIMATION_DURATION
    private var mAnimatorUpdateListener: ValueAnimator.AnimatorUpdateListener? =
            null

    /**
     * 进度阴影
     */
    private var mShadowPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mShadowColor = Color.parseColor("#2b1a14")
    private val mShadowColor2 = Color.parseColor("#102b1a14")
    var mShowShadow = true

    /**
     * 矩形
     */
    private val mHorizontalRectF = RectF()
    private val mVerticalRectF = RectF()
    private val mRingRectF = RectF()
    private val mSectorRectF = RectF()

    /**
     * 初始化自定义属性
     */
    private fun initAttributes(
            context: Context,
            attrs: AttributeSet,
            defStyleAttr: Int
    ) {
        val attributes = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.SmartProgressBar,
                defStyleAttr,
                0
        )
        try {
            max = attributes.getFloat(
                    R.styleable.SmartProgressBar_spb_max,
                    DEFAULT_MAX
            )
            progress =
                    attributes.getFloat(R.styleable.SmartProgressBar_spb_progress, 0f)
            mProgressStartColor = attributes.getColor(
                    R.styleable.SmartProgressBar_spb_progress_start_color,
                    DEFAULT_PROGRESS_COLOR
            )
            mProgressCenterColor = attributes.getColor(
                    R.styleable.SmartProgressBar_spb_progress_center_color,
                    DEFAULT_PROGRESS_COLOR
            )
            mProgressEndColor = attributes.getColor(
                    R.styleable.SmartProgressBar_spb_progress_end_color,
                    DEFAULT_PROGRESS_COLOR
            )
            mProgressColorsResId = attributes.getResourceId(
                    R.styleable.SmartProgressBar_spb_progress_colors,
                    0
            )
            mProgressPositionsResId = attributes.getResourceId(
                    R.styleable.SmartProgressBar_spb_progress_positions,
                    0
            )
            mProgressBarBgColor = attributes.getColor(
                    R.styleable.SmartProgressBar_spb_progress_bar_bg_color,
                    DEFAULT_PROGRESS_BAR_BG_COLOR
            )
            mProgressBarBgGradient = attributes.getBoolean(
                    R.styleable.SmartProgressBar_spb_progress_bar_bg_gradient,
                    false
            )
            mProgressBarBgAlpha = attributes.getFloat(
                    R.styleable.SmartProgressBar_spb_progress_bar_bg_alpha,
                    1f
            )
            mIsShowPercentText = attributes.getBoolean(
                    R.styleable.SmartProgressBar_spb_show_percent_text,
                    false
            )
            mIsShowPercentSign = attributes.getBoolean(
                    R.styleable.SmartProgressBar_spb_show_percent_sign,
                    false
            )
            mPercentTextColor = attributes.getColor(
                    R.styleable.SmartProgressBar_spb_percent_text_color,
                    DEFAULT_PERCENT_TEXT_COLOR
            )
            mPercentTextSize = attributes.getDimension(
                    R.styleable.SmartProgressBar_spb_percent_text_size,
                    DEFAULT_PERCENT_TEXT_SIZE
            )
            mBorderColor = attributes.getColor(
                    R.styleable.SmartProgressBar_spb_border_color,
                    DEFAULT_BORDER_COLOR
            )
            mBorderWidth = attributes.getDimension(
                    R.styleable.SmartProgressBar_spb_border_width,
                    0f
            )
            mRadius = attributes.getDimension(
                    R.styleable.SmartProgressBar_spb_radius,
                    0f
            )
            mClockwise = attributes.getBoolean(
                    R.styleable.SmartProgressBar_spb_clockwise,
                    true
            )
            mTopLeftRadius = attributes.getDimension(
                    R.styleable.SmartProgressBar_spb_top_left_radius,
                    0f
            )
            mTopRightRadius = attributes.getDimension(
                    R.styleable.SmartProgressBar_spb_top_right_radius,
                    0f
            )
            mBottomLeftRadius = attributes.getDimension(
                    R.styleable.SmartProgressBar_spb_bottom_left_radius,
                    0f
            )
            mBottomRightRadius = attributes.getDimension(
                    R.styleable.SmartProgressBar_spb_bottom_right_radius,
                    0f
            )
            mShapeStyle =
                    attributes.getInt(R.styleable.SmartProgressBar_spb_shape_style, 0)
            mIsAnimated = attributes.getBoolean(
                    R.styleable.SmartProgressBar_spb_animated,
                    true
            )
            mDuration = attributes.getInt(
                    R.styleable.SmartProgressBar_spb_animated_duration,
                    DEFAULT_ANIMATION_DURATION.toInt()
            ).toLong()
            mShowShadow = attributes.getBoolean(R.styleable.SmartProgressBar_spb_show_shadow, false)
            if (max <= 0) {
                max = DEFAULT_MAX
            }
            if (progress > max) {
                progress = max
            } else if (progress < 0) {
                progress = 0f
            }
        } finally {
            attributes.recycle()
        }
    }

    /**
     * 初始化
     */
    private fun init() {
        // 硬件加速
        setLayerType(LAYER_TYPE_HARDWARE, null)

        /*进度画笔*/
        mProgressPaint.style = Paint.Style.FILL

        /*进度条背景画笔*/
        mProgressBarBgPaint.color = mProgressBarBgColor

        /*边框画笔*/
        mBorderPaint.color = mBorderColor
        mBorderPaint.style = Paint.Style.STROKE
        mBorderPaint.strokeWidth = mBorderWidth

        /*进度百分比字体画笔*/
        mPercentTextPaint.color = mPercentTextColor
        mPercentTextPaint.style = Paint.Style.FILL
        mPercentTextPaint.textSize = mPercentTextSize

        /*若是设置了radius属性，四个圆角属性值以radius属性值为准*/
        if (mRadius > 0) {
            mBottomRightRadius = mRadius
            mBottomLeftRadius = mBottomRightRadius
            mTopRightRadius = mBottomLeftRadius
            mTopLeftRadius = mTopRightRadius
        }

        /*圆角的半径，依次为左上角xy半径，右上角，右下角，左下角*/
        mRadii = floatArrayOf(
                mTopLeftRadius, mTopLeftRadius, mTopRightRadius, mTopRightRadius,
                mBottomRightRadius, mBottomRightRadius, mBottomLeftRadius, mBottomLeftRadius
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // 是否执行动画
        if (mIsAnimated) {
            // 开始动画
            startAnimating()
        }
    }

    /**
     * 开始动画
     */
    private fun startAnimating() {
        if (mAnimator == null) {
            mAnimator = ValueAnimator()
            mAnimator = ValueAnimator.ofFloat(0f, progress)
            mAnimator!!.repeatCount = 0
            mAnimator!!.repeatMode = ValueAnimator.RESTART
            mAnimator!!.interpolator = LinearInterpolator()
            mAnimator!!.duration = mDuration
            // 设置动画的回调
            mAnimatorUpdateListener =
                    ValueAnimator.AnimatorUpdateListener { animation ->
                        progress = animation.animatedValue as Float
                        post { this.postInvalidate() }
                    }
            mAnimator!!.addUpdateListener(mAnimatorUpdateListener)
        }
        post { mAnimator!!.start() }
    }

    /**
     * 停止动画
     */
    private fun pauseAnimating() {
        mAnimator?.pause()
    }

    /**
     * 取消动画
     */
    private fun cancelAnimating() {
        mAnimator?.cancel()
    }

    private fun isAnimatorRunning(): Boolean {
        return mAnimator?.isRunning ?: false
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 取消动画
        cancelAnimating()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        val width: Int
        val height: Int
        width = when (widthSpecMode) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> {
                dp2px(context, DEFAULT_WIDTH)
            }
            else -> {
                widthSpecSize
            }
        }
        height = when (heightSpecMode) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> {
                dp2px(context, DEFAULT_HEIGHT)
            }
            else -> {
                heightSpecSize
            }
        }
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCenterX = width.toFloat() / 2
        mCenterY = height.toFloat() / 2
        setProgressColorsResId(mProgressColorsResId)
        setProgressPositionsResId(mProgressPositionsResId)
        when (mProgressColors) {
            null -> {
                mProgressColors = IntArray(5)
                mProgressColors!![0] = mProgressStartColor
                mProgressColors!![1] = mProgressStartColor
                mProgressColors!![2] = mProgressCenterColor
                mProgressColors!![3] = mProgressEndColor
                mProgressColors!![4] = mProgressEndColor
                mProgressPositions = FloatArray(5)
                mProgressPositions!![0] = 0.0f
                mProgressPositions!![1] = 0.1f
                mProgressPositions!![2] = 0.5f
                mProgressPositions!![3] = 0.9f
                mProgressPositions!![4] = 1.0f
            }
        }
        when (mShapeStyle) {
            ShapeStyle.HORIZONTAL -> {
                /*创建线性颜色渐变器*/
                val linearGradient: Shader = LinearGradient(
                        mBorderWidth,
                        (height - mBorderWidth * 2) / 2,
                        mBorderWidth + progress / max * (width - mBorderWidth * 2),
                        (height - mBorderWidth * 2) / 2,
                        mProgressColors!!,
                        mProgressPositions, Shader.TileMode.MIRROR
                )
                mProgressPaint.shader = linearGradient
            }
            ShapeStyle.VERTICAL -> {
                /*创建线性颜色渐变器*/
                val linearGradient: Shader = LinearGradient(
                        (width - mBorderWidth * 2) / 2,
                        height - mBorderWidth,
                        (width - mBorderWidth * 2) / 2,
                        height - progress / max * (height - mBorderWidth * 2) - mBorderWidth,
                        mProgressColors!!,
                        mProgressPositions, Shader.TileMode.MIRROR
                )
                mProgressPaint.shader = linearGradient
            }
            ShapeStyle.RING -> {
                /*创建扫描式渐变器*/
                if (!mClockwise) {
                    reverseProgressColors()
                }
                val sweepGradient = SweepGradient(
                        0f,
                        0f,
                        mProgressColors!!,
                        mProgressPositions
                )
                mProgressPaint.shader = sweepGradient
                if (mProgressBarBgGradient) {
                    mProgressBarBgPaint.shader = sweepGradient
                    mProgressBarBgPaint.alpha = (mProgressBarBgAlpha * 255).toInt()
                }
                val gradientMatrix = Matrix()
                gradientMatrix.setTranslate(mCenterX, mCenterY)
                sweepGradient.setLocalMatrix(gradientMatrix)
            }
            ShapeStyle.SECTOR -> {
                /*创建扫描式渐变器*/
                if (!mClockwise) {
                    reverseProgressColors()
                }
                val sweepGradient: Shader = SweepGradient(
                        0f,
                        0f,
                        mProgressColors!!,
                        null
                )
                mProgressPaint.shader = sweepGradient
                val gradientMatrix = Matrix()
                gradientMatrix.setTranslate(mCenterX, mCenterY)
                sweepGradient.setLocalMatrix(gradientMatrix)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        when (mShapeStyle) {
            ShapeStyle.HORIZONTAL -> {
                drawHorizontalProgressBar(canvas)
            }
            ShapeStyle.VERTICAL -> {
                drawVerticalProgressBar(canvas)
            }
            ShapeStyle.RING -> {
                drawRingProgressBar(canvas)
            }
            ShapeStyle.SECTOR -> {
                drawSectorProgressBar(canvas)
            }
        }
        canvas.restore()
        super.onDraw(canvas)
    }

    /**
     * 避免View占用额外的GPU内存空间
     */
    override fun hasOverlappingRendering(): Boolean {
        return false
    }

    /**
     * 绘制水平进度条
     *
     * @param canvas 画布
     */
    private fun drawHorizontalProgressBar(canvas: Canvas) {
        // 绘制进度条背景
        mProgressBarBgPath.rewind()
        mHorizontalRectF.left = mBorderWidth / 2
        mHorizontalRectF.top = mBorderWidth / 2
        mHorizontalRectF.right = width - mBorderWidth / 2
        mHorizontalRectF.bottom = height - mBorderWidth / 2
        mProgressBarBgPath.addRoundRect(
                mHorizontalRectF,
                mRadii,
                Path.Direction.CW
        )
        canvas.drawPath(mProgressBarBgPath, mProgressBarBgPaint)

        // 绘制边框
        if (mBorderWidth > 0) {
            mBorderPath.rewind()
            mBorderPath.addRoundRect(mHorizontalRectF, mRadii, Path.Direction.CW)
            canvas.drawPath(mBorderPath, mBorderPaint)
        }


        // 绘制进度
        mProgressPath.rewind()
        mHorizontalRectF.left = mBorderWidth
        mHorizontalRectF.top = mBorderWidth
        mHorizontalRectF.right = mBorderWidth + progress / max * (width - mBorderWidth * 2)
        mHorizontalRectF.bottom = height - mBorderWidth
        mProgressPath.addRoundRect(mHorizontalRectF, mRadii, Path.Direction.CW)
        canvas.drawPath(mProgressPath, mProgressPaint)
        if (mIsShowPercentText) {
            // 绘制进度文字和进度百分比符号
            drawPercentText(canvas)
        }
    }

    /**
     * 绘制竖直进度条
     *
     * @param canvas 画布
     */
    private fun drawVerticalProgressBar(canvas: Canvas) {
        // 绘制进度条背景
        mProgressBarBgPath.rewind()
        mVerticalRectF.left = mBorderWidth / 2
        mVerticalRectF.top = mBorderWidth / 2
        mVerticalRectF.right = width - mBorderWidth / 2
        mVerticalRectF.bottom = height - mBorderWidth / 2
        mProgressBarBgPath.addRoundRect(mVerticalRectF, mRadii, Path.Direction.CW)
        canvas.drawPath(mProgressBarBgPath, mProgressBarBgPaint)

        // 绘制边框
        if (mBorderWidth > 0) {
            mBorderPath.rewind()
            mBorderPath.addRoundRect(mVerticalRectF, mRadii, Path.Direction.CW)
            canvas.drawPath(mBorderPath, mBorderPaint)
        }

        // 绘制进度
        mProgressPath.rewind()
        mVerticalRectF.left = mBorderWidth
        mVerticalRectF.top =
                height - progress / max * (height - mBorderWidth * 2) - mBorderWidth
        mVerticalRectF.right = width - mBorderWidth
        mVerticalRectF.bottom = height - mBorderWidth
        mProgressPath.addRoundRect(mVerticalRectF, mRadii, Path.Direction.CW)
        canvas.drawPath(mProgressPath, mProgressPaint)
        if (mIsShowPercentText) {
            // 绘制进度文字和进度百分比符号
            drawPercentText(canvas)
        }
    }

    /**
     * 绘制圆环进度条
     *
     * @param canvas 画布
     */
    private fun drawRingProgressBar(canvas: Canvas) {
        val strokeWidth = mCenterX - mRadius - mBorderWidth
        // 绘制边框
        if (mBorderWidth > 0) {
            mBorderPath.rewind()
            mBorderPath.addCircle(
                    mCenterX,
                    mCenterY,
                    mCenterX - mBorderWidth / 2,
                    Path.Direction.CW
            )
            canvas.drawPath(mBorderPath, mBorderPaint)
        }

        // 逆时针旋转画布90度
        canvas.rotate(-90f, mCenterX, mCenterY)
        mRingRectF.left = mBorderWidth + strokeWidth / 2
        mRingRectF.top = mBorderWidth + strokeWidth / 2
        mRingRectF.right = width - mBorderWidth - strokeWidth / 2
        mRingRectF.bottom = height - mBorderWidth - strokeWidth / 2

        // 绘制进度条背景
        mProgressBarBgPaint.style = Paint.Style.STROKE
        mProgressBarBgPaint.strokeCap = Paint.Cap.ROUND
        mProgressBarBgPaint.strokeJoin = Paint.Join.ROUND
        mProgressBarBgPaint.strokeWidth = strokeWidth
        mProgressBarBgPath.rewind()
        if (mClockwise) {
            mProgressBarBgPath.addArc(mRingRectF, 0f, 360f)
        } else {
            mProgressBarBgPath.addArc(mRingRectF, 0f, -360f)
        }
        canvas.drawPath(mProgressBarBgPath, mProgressBarBgPaint)

        // 添加阴影效果
        if (mShowShadow) {
            mShadowPaint.style = Paint.Style.STROKE
            mShadowPaint.strokeCap = Paint.Cap.ROUND
            mShadowPaint.strokeJoin = Paint.Join.ROUND
            mShadowPaint.strokeWidth = strokeWidth
            mShadowPaint.color = mShadowColor
            mShadowPath.rewind()
            if (progress / max <= 0.92f) {
                if (mClockwise) {
                    mShadowPath.addArc(mRingRectF, 0f, 360 * progress / max)
                } else {
                    mShadowPath.addArc(mRingRectF, 0f, -360 * progress / max)
                }
            } else {
                mShadowPath.addArc(mRingRectF, 0f, 1f)
            }
            mShadowPaint.setShadowLayer(20f, 0f, 0f, mShadowColor)
            canvas.drawPath(mShadowPath, mShadowPaint)
            mShadowPaint.setShadowLayer(30f, 0f, 0f, mShadowColor2)
            canvas.drawPath(mShadowPath, mShadowPaint)
        }

        // 绘制进度
        mProgressPaint.style = Paint.Style.STROKE
        mProgressPaint.strokeCap = Paint.Cap.ROUND
        mProgressPaint.strokeJoin = Paint.Join.ROUND
        mProgressPaint.strokeWidth = strokeWidth
        mProgressPath.rewind()
        if (mClockwise) {
            mProgressPath.addArc(mRingRectF, 0f, 360 * progress / max)
        } else {
            mProgressPath.addArc(mRingRectF, 0f, -360 * progress / max)
        }
        canvas.drawPath(mProgressPath, mProgressPaint)

        // 绘制开始状态时的圆
        mStartProgressPaint.style = Paint.Style.STROKE
        mStartProgressPaint.strokeCap = Paint.Cap.ROUND
        mStartProgressPaint.strokeJoin = Paint.Join.ROUND
        mStartProgressPaint.strokeWidth = strokeWidth
        if (mClockwise) {
            mStartProgressPaint.color = mProgressColors!![0]
        } else {
            mStartProgressPaint.color = mProgressColors!![mProgressColors!!.size - 1]
        }
        mStartProgressPath.rewind()
        mStartProgressPath.addArc(mRingRectF, 0f, 1f)
        canvas.drawPath(mStartProgressPath, mStartProgressPaint)

        // 结束阶段进度
        mEndProgressPaint.style = Paint.Style.STROKE
        mEndProgressPaint.strokeCap = Paint.Cap.ROUND
        mEndProgressPaint.strokeJoin = Paint.Join.ROUND
        mEndProgressPaint.strokeWidth = strokeWidth
        if (mClockwise) {
            mEndProgressPaint.color = mProgressColors!![mProgressColors!!.size - 1]
        } else {
            mEndProgressPaint.color = mProgressColors!![0]
        }
        if (progress / max > 0.9f) {
            // 添加阴影效果
            if (mShowShadow && progress / max > 0.92f) {
                mShadowPath.rewind()
                if (mClockwise) {
                    mShadowPath.addArc(mRingRectF, 360 * (progress / max) - 1, 1f)
                } else {
                    mShadowPath.addArc(mRingRectF, -360 * (progress / max) + 1, -1f)
                }
                mShadowPaint.setShadowLayer(20f, 0f, 0f, mShadowColor)
                canvas.drawPath(mShadowPath, mShadowPaint)
                mShadowPaint.setShadowLayer(30f, 0f, 0f, mShadowColor2)
                canvas.drawPath(mShadowPath, mShadowPaint)
            }

            mEndProgressPath.rewind()
            if (mClockwise) {
                mEndProgressPath.addArc(mRingRectF, 360 * 0.9f, 360 * (progress / max - 0.9f))
            } else {
                mEndProgressPath.addArc(
                        mRingRectF,
                        -360 * 0.9f,
                        -360 * (progress / max - 0.9f)
                )
            }
            canvas.drawPath(mEndProgressPath, mEndProgressPaint)
        }
        if (mIsShowPercentText) {
            // 顺时针旋转画布90度
            canvas.rotate(90f, mCenterX, mCenterY)
            // 绘制进度文字和进度百分比符号
            drawPercentText(canvas)
        }
    }

    /**
     * 绘制扇形扫描式进度
     *
     * @param canvas 画布
     */
    private fun drawSectorProgressBar(canvas: Canvas) {
        // 绘制进度条背景
        canvas.drawCircle(mCenterX, mCenterY, mCenterX - mBorderWidth, mProgressBarBgPaint)

        // 绘制边框
        if (mBorderWidth > 0) {
            mBorderPath.rewind()
            mBorderPath.addCircle(
                    mCenterX,
                    mCenterY,
                    mCenterX - mBorderWidth / 2,
                    Path.Direction.CW
            )
            canvas.drawPath(mBorderPath, mBorderPaint)
        }

        // 绘制进度
        // 逆时针旋转画布90度
        canvas.rotate(-90f, mCenterX, mCenterY)
        mSectorRectF.left = mBorderWidth
        mSectorRectF.top = mBorderWidth
        mSectorRectF.right = width - mBorderWidth
        mSectorRectF.bottom = height - mBorderWidth
        if (mClockwise) {
            canvas.drawArc(mSectorRectF, 0f, 360 * progress / max, true, mProgressPaint)
        } else {
            canvas.drawArc(mSectorRectF, 0f, -360 * progress / max, true, mProgressPaint)
        }
        if (mIsShowPercentText) {
            // 顺时针旋转画布90度
            canvas.rotate(90f, mCenterX, mCenterY)
            // 绘制进度文字和进度百分比符号
            drawPercentText(canvas)
        }
    }

    /**
     * 绘制进度文字和进度百分比符号
     *
     * @param canvas 画布
     */
    private fun drawPercentText(canvas: Canvas) {
        var percent = (progress * 100 / max).toInt().toString()
        if (mIsShowPercentSign) {
            percent = "$percent%"
        }
        val rect = Rect()
        // 获取字符串的宽高值
        mPercentTextPaint.getTextBounds(percent, 0, percent.length, rect)
        var textWidth = rect.width().toFloat()
        var textHeight = rect.height().toFloat()
        if (textWidth >= width) {
            textWidth = width.toFloat()
        }
        if (textHeight >= height) {
            textHeight = height.toFloat()
        }
        canvas.drawText(
                percent,
                mCenterX - textWidth / 2,
                mCenterY + textHeight / 2,
                mPercentTextPaint
        )
    }

    internal class SavedState : BaseSavedState {
        internal var progress = 0F

        internal constructor(superState: Parcelable) : super(superState) {}
        private constructor(`in`: Parcel) : super(`in`) {
            progress = `in`.readFloat()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeFloat(progress)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    public override fun onSaveInstanceState(): Parcelable? {
        // Force our ancestor class to save its state
        val superState = super.onSaveInstanceState()
        val ss = superState?.let { SavedState(it) }
        ss?.progress = progress
        return ss
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        setProgress(ss.progress)
    }

    fun setProgressBarBgColor(mProgressBarBgColor: Int): SmartProgressBar {
        this.mProgressBarBgColor = mProgressBarBgColor
        mProgressBarBgPaint.color = mProgressBarBgColor
        return this
    }

    fun setProgressBarBgGradient(mProgressBarBgGradient: Boolean): SmartProgressBar {
        this.mProgressBarBgGradient = mProgressBarBgGradient
        return this
    }

    fun setProgressBarBgAlpha(mProgressBarBgAlpha: Float): SmartProgressBar {
        this.mProgressBarBgAlpha = mProgressBarBgAlpha
        mProgressBarBgPaint.alpha = (mProgressBarBgAlpha * 255).toInt()
        return this
    }

    fun setProgressStartColor(mProgressStartColor: Int): SmartProgressBar {
        this.mProgressStartColor = mProgressStartColor
        return this
    }

    fun setProgressCenterColor(mProgressCenterColor: Int): SmartProgressBar {
        this.mProgressCenterColor = mProgressCenterColor
        return this
    }

    fun setProgressEndColor(mProgressEndColor: Int): SmartProgressBar {
        this.mProgressEndColor = mProgressEndColor
        return this
    }

    fun setProgressColorsResId(mProgressColorsResId: Int): SmartProgressBar {
        this.mProgressColorsResId = mProgressColorsResId
        if (mProgressColorsResId != 0) {
            try {
                val colors = context.resources.getStringArray(mProgressColorsResId)
                mProgressColors = IntArray(colors.size)
                for (i in colors.indices) {
                    mProgressColors!![i] = Color.parseColor(colors[i])
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return this
    }

    fun reverseProgressColors(): SmartProgressBar {
        if (mProgressColors != null) {
            val tempProgressColors = IntArray(mProgressColors!!.size)
            for (i in mProgressColors!!.indices) {
                tempProgressColors[mProgressColors!!.size - 1 - i] = mProgressColors!![i]
            }
            mProgressColors = tempProgressColors
        }
        return this
    }

    fun setProgressPositionsResId(mProgressPositionsResId: Int): SmartProgressBar {
        this.mProgressPositionsResId = mProgressPositionsResId
        if (mProgressPositionsResId != 0) {
            try {
                val positions =
                        context.resources.getIntArray(mProgressPositionsResId)
                mProgressPositions = FloatArray(positions.size)
                for (i in positions.indices) {
                    mProgressPositions!![i] = positions[i] / 100f
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return this
    }

    fun setBorderColor(mBorderColor: Int): SmartProgressBar {
        this.mBorderColor = mBorderColor
        mBorderPaint.color = mBorderColor
        return this
    }

    fun setBorderWidth(mBorderWidth: Float): SmartProgressBar {
        this.mBorderWidth = mBorderWidth
        mBorderPaint.strokeWidth = mBorderWidth
        return this
    }

    fun setPercentTextSize(mPercentTextSize: Float): SmartProgressBar {
        this.mPercentTextSize = mPercentTextSize
        mPercentTextPaint.textSize = mPercentTextSize
        return this
    }

    fun setPercentTextColor(mPercentTextColor: Int): SmartProgressBar {
        this.mPercentTextColor = mPercentTextColor
        mPercentTextPaint.color = mPercentTextColor
        return this
    }

    fun setShapeStyle(mShapeStyle: Int): SmartProgressBar {
        this.mShapeStyle = mShapeStyle
        return this
    }

    fun setRadius(mRadius: Float): SmartProgressBar {
        this.mRadius = mRadius
        setRadius(mRadius, mRadius, mRadius, mRadius)
        return this
    }

    fun setClockwise(mClockwise: Boolean): SmartProgressBar {
        this.mClockwise = mClockwise
        return this
    }

    fun setRadius(
            mTopLeftRadius: Float,
            mTopRightRadius: Float,
            mBottomRightRadius: Float,
            mBottomLeftRadius: Float
    ): SmartProgressBar {
        this.mTopLeftRadius = mTopLeftRadius
        this.mTopRightRadius = mTopRightRadius
        this.mBottomRightRadius = mBottomRightRadius
        this.mBottomLeftRadius = mBottomLeftRadius
        return this
    }

    fun setRadii(mRadii: FloatArray): SmartProgressBar {
        this.mRadii = mRadii
        return this
    }

    fun setIsShowPercentSign(mIsShowPercentSign: Boolean): SmartProgressBar {
        this.mIsShowPercentSign = mIsShowPercentSign
        return this
    }

    fun setIsShowPercentText(mIsShowPercentText: Boolean): SmartProgressBar {
        this.mIsShowPercentText = mIsShowPercentText
        return this
    }

    fun setIsAnimated(mIsAnimated: Boolean): SmartProgressBar {
        this.mIsAnimated = mIsAnimated
        return this
    }

    fun setDuration(mDuration: Long): SmartProgressBar {
        this.mDuration = mDuration
        return this
    }

    fun setAnimatorUpdateListener(mAnimatorUpdateListener: ValueAnimator.AnimatorUpdateListener?): SmartProgressBar {
        this.mAnimatorUpdateListener = mAnimatorUpdateListener
        return this
    }

    fun setMax(max: Float): SmartProgressBar {
        this.max = max
        return this
    }

    fun getMax(): Float {
        return max
    }

    fun setProgress(progress: Float): SmartProgressBar {
        if (isAnimatorRunning()) {
            return this
        }
        when (val mLastProgress = this.progress) {
            0f, max -> {
                when {
                    progress > max -> {
                        this.progress = max
                    }
                    progress < 0 -> {
                        this.progress = 0f
                    }
                    else -> {
                        this.progress = progress
                    }
                }
                post { this.postInvalidate() }
                return this
            }
            else -> {
                mAnimator = ValueAnimator.ofFloat(mLastProgress, progress)
                mAnimator!!.interpolator = LinearInterpolator()
                mAnimator!!.duration = 2000
                mAnimator!!.addUpdateListener(this)
                mAnimator!!.start()
                return this
            }
        }
    }

    fun getProgress(): Float {
        return progress
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        post {
            val animatedValue = animation.animatedValue as Float
            progress = when {
                animatedValue > max -> {
                    max
                }
                animatedValue < 0 -> {
                    0f
                }
                else -> {
                    animatedValue
                }
            }
            postInvalidate()
        }
    }

    /**
     * dp转px
     *
     * @param dpValue dp值
     * @return px值
     */
    private fun dp2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * px转dp
     *
     * @param pxValue px值
     * @return dp值
     */
    private fun px2dp(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    companion object {
        private const val DEFAULT_WIDTH = 100f
        private const val DEFAULT_HEIGHT = 100f
        private const val DEFAULT_PROGRESS_COLOR = Color.BLUE
        private const val DEFAULT_PROGRESS_BAR_BG_COLOR = Color.WHITE
        private const val DEFAULT_PERCENT_TEXT_COLOR = Color.BLACK
        private const val DEFAULT_PERCENT_TEXT_SIZE = 15f
        private const val DEFAULT_BORDER_COLOR = Color.RED
        private const val DEFAULT_MAX = 100f
        private const val DEFAULT_ANIMATION_DURATION = 1000L
    }

    init {
        attrs?.let { initAttributes(context, it, defStyleAttr) }
        init()
    }
}