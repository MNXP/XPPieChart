# XPPieChart
饼状图
![image](https://github.com/MNXP/XPPieChart/blob/master/image/12.gif)

    增加立体效果，提取配置参数
```
<declare-styleable name="CakeSurfaceView">
        <attr name="isDrawByAnim" format="boolean"/>//是否动画
        <attr name="isSolid" format="boolean"/>//是否立体
        <attr name="duration" format="integer|reference"/>//动画时间
        <attr name="defaultColor" format="string"/>//默认颜色

        <attr name="ringWidth" format="integer|reference"/>//圆环宽度
        <attr name="solidWidth" format="integer|reference"/>//立体宽度
        <attr name="fineTuningWidth" format="integer|reference"/>//微调宽度
    </declare-styleable>
```
    xml中使用
```
<com.xp.xppiechart.view.CakeSurfaceView
            android:id="@+id/assets_pie_chart"
            android:background="#ffffff"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            app:defaultColor="#ff8712"
            app:ringWidth="20"
            app:solidWidth="5"
            app:duration="3000"
            app:isSolid="true"
            app:isDrawByAnim="true"/>
```
***
以上就是简单的实现动态绘制饼状图，待完善，以后会更新。如有建议和意见，请及时沟通。
