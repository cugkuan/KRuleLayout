
KRuleLayout是一种新布局，解决某些情况下布局问题

# 1.背景
Android提供的几种布局方式能满足绝大部分的需求，但是有些特殊的布局需求，要么需要多个布局的叠加，层层嵌套，要么根本无法直接实现，需要自己去写布局代码；在工作中，我碰到了这样的需求：

![image])(https://github.com/cugkuan/KRuleLayout/blob/master/pic/1.jpeg)

![image](https://github.com/cugkuan/KRuleLayout/blob/master/pic/2.jpeg)

![image](https://github.com/cugkuan/KRuleLayout/blob/master/pic/3.jpeg)


 这三个 item 都是问题；每一个问题有问题标题，问题描述，问题图片，问题的答案数，标签，还有“回答”这个操作按钮。
对于一个问题，问题的描述可能有，可能没有，问题的图片也可能有，也可能没有。
注意上面三个item的表现，问题的描述 和最后一行，根据情况，或显示在标题的下面的左半部分，也有可能单独成行。

那这种布局如何实现？
显然依靠Android系统提供的布局方式，将无法满足这个需求。需要自己去写布局，而KRulayout布局就是解决这样问题的。如果你碰到这样的需求，可以直接使用KRuleLayout而不必再去重复发明轮子。

KRuleLayout可以完成下面样式的布局

 ![image](https://github.com/cugkuan/KRuleLayout/blob/master/pic/4.png)

其中最后一个布局，以right为布局标准。

# 2.用法
KRuleLayout，将View分为三类，分别是标记为left,right和没有标记的View，没有标记的View的布局位置，取决于left和right的高度。
使用KRuleLayout的时候，需要指定其中一个View为left,一个为 right,当然也可以不指定left,right，或者只指定其中的一个（如只有left没有right或者只有right没有left的View）。当没有指定任何View为left和right的时候，KRuleLayout的表现跟LinearLayout差不多了。

### 下面是KRuleLayout的属性列表


   属性 | 取值 | 备注 |
| ------ | ------ | ------ |
| rule | left,right,bottom | 指定子元素的位置。其中当View没有指定位置的时候，默认为是bottom。 |
| direction | left,right | KRuleLayout的布局方向，见示例|
|ignoreHeight|dimension|有些情况下，即使是left或者right还有空间，但是我们希望其他的view 不必在left或者righ下面布局。ignoreHeight  = 20dp ,意思是，即使left或者right的空间剩下的不足20dp,那么其他元素就单独的一行，不需要在left或者right下面了。|
|android:layout_weigh|float|eft和Right的宽度会根据这个权重进行动态的就算分配|
