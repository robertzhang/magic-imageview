# magic-imageview
make a powerful imageview which you could used anywhere

#### 本工程旨在做一个功能强大的Imageview，将一些常见Imageview功能整合到一起。

### 1.全屏现实的ImageView控件MaxScaleImageView
* 该组件继承自MagicImageView。值得一提的是MagicImageView继承自android.widget.ImageView，并没有做任何处理。她的存在是为了将所有自定义的ImageView整合起来

* MaxScaleImageView中关键的方法是updateScale().用于计算和设置图片的尺寸

### 2.可缩放的ImageView控件ZoomImageView
* 该控件也继承自MagicImageView，控件主要是用于图片的缩放，可以用于图片查看的时候，将图片放大来查看细节。图片放大后四个边不会有空隙。另外，双击图片可以放大局部，再双击可以缩小。
