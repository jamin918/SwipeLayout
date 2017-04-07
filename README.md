# SwipeLayout
感谢ssyijiu，在[ssyijiu/SwipeLayou](https://github.com/ssyijiu/SwipeLayout)的基础上修改，处理了多指侧滑的情况和删除时item复用导致的显示问题。截图太大显得有点卡，请下载源码体验，实际效果还是不错的，喜欢的话给个star,谢谢。<br><br>  ![效果截图](https://github.com/jamin918/gif_repository/blob/master/swipe_delete.gif)

  ![](https://github.com/jamin918/gif_repository/blob/master/swipe_delete2.gif)
  
# Usage  

## xml中引用  
 在xml中引用SwipeLayout:
 ```xml
 <RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:swipe="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.jm.swipe_lib.SwipeLayout
        android:id="@+id/swipelayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        swipe:show_mode="lay_down">

        <!--内容区域-->
        <include layout="@layout/layout_content" />

        <!--删除区域-->
        <include layout="@layout/layout_delete" />

    </com.jm.swipe_lib.SwipeLayout>

</RelativeLayout>
 ```
 
 ## 代码使用
 item点击事件：
 ```java
              swipeLayout.setOnItemClickListener(new SwipeLayout.OnItemClickListener() {
                @Override
                public void onItemClick() {
                    Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
                }
            });
  ```
    
    
 删除布局点击事件:
 ```java
               swipeLayout.getDeleteView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SwipeLayoutManager.getInstance().quickClose();
                    mTestData.remove(s);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(mContext, "数据总数: " + mTestData.size(), Toast.LENGTH_SHORT).show();
                }
            });
  ```
