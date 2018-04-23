package com.loc8r.seattle.utils;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;


public class POIStampDecoration extends RecyclerView.ItemDecoration {
    private Drawable mDivider;

    public POIStampDecoration(Drawable divider) {
        mDivider = divider;
    }

    private boolean isOnLeft(int position){
        return (position % 2 == 0);
    }

    /**
     * Draw any appropriate decorations into the Canvas supplied to the RecyclerView.
     * Any content drawn by this method will be drawn before the item views are drawn,
     * and will thus appear underneath the views.
     *
     * @param canvas      Canvas to draw into
     * @param parent RecyclerView this ItemDecoration is drawing into
     * @param state  The current state of RecyclerView
     */
    @Override public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(canvas, parent, state);

        final int extraVine = 2;

        int childCount = parent.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            int listPosition = (int) child.getTag() - 1;

            // Initialize Bounds for the decorations
            int decoratorLeft = 0;
            int decoratorRight = 0;
            int decoratorTop = child.getTop();
            int decoratorBottom = child.getBottom();

            // Special case, if we're on the first item in the list,
            // make it only half height starting at middle of view
            if(listPosition == 0){
                decoratorTop = (int) Math.floor ((child.getTop() + child.getBottom())/2) - extraVine; // To fix a minor graphical bug at top of "POI vine" in view
            }

            // Special case, if we're the first item on the right
            // This helps align the 2 collection "vine" lines at the top
            if(listPosition == 1){
                decoratorTop -= extraVine; // To fix a minor graphical bug at top of "POI vine" in view
            }

            // Special case, for the last 2 items in the list
            if(i == childCount-2){
                decoratorBottom += extraVine;
            }
            if(i == childCount-1){
                decoratorBottom = (int) Math.floor ((child.getTop() + child.getBottom())/2) + extraVine;
            }

            // RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            if(isOnLeft(listPosition)){
                decoratorRight = child.getRight();
                decoratorLeft = decoratorRight - mDivider.getIntrinsicWidth();
                Log.d("Intrinsic", "divider width says: " + String.valueOf(mDivider.getIntrinsicWidth()));
            } else {
                decoratorLeft = child.getLeft();
                decoratorRight = child.getLeft() + mDivider.getIntrinsicWidth();
            }

            mDivider.setBounds(decoratorLeft, decoratorTop, decoratorRight, decoratorBottom);
            mDivider.draw(canvas);

        }

    }

    /**
     * Retrieve any offsets for the given item. Each field of <code>outRect</code> specifies
     * the number of pixels that the item view should be inset by, similar to padding or margin.
     * The default implementation sets the bounds of outRect to 0 and returns.
     * <p>
     * <p>
     * If this ItemDecoration does not affect the positioning of item views, it should set
     * all four fields of <code>outRect</code> (left, top, right, bottom) to zero
     * before returning.
     * <p>
     * <p>
     * If you need to access Adapter for additional data, you can call
     * {@link RecyclerView#getChildAdapterPosition(View)} to get the adapter position of the
     * View.
     *
     *  See: https://stackoverflow.com/questions/29146781/decorating-recyclerview-with-gridlayoutmanager-to-display-divider-between-item
     *
     * @param outRect Rect to receive the output.
     * @param view    The child view to decorate
     * @param parent  RecyclerView this ItemDecoration is decorating
     * @param state   The current state of RecyclerView.
     */
    @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        // good example here: https://stackoverflow.com/questions/29666598/android-recyclerview-finding-out-first-and-last-view-on-itemdecoration/30404499#30404499

        /**
         *  Special case.  Te first right side item in the list should have an extra 50% top offset so that these equal sized views are perfectly staggered.
         */
        if (parent.getChildAdapterPosition(view) == 1) {

            /**
             *  We would normally do a outRect.top = view.getHeight()/2 to create a 50% top offset on the first right item in the list.
             *  However, problems would arise if we paused the app when the top right item was scrolled off screen.
             *  In this situation, when we re-inflated the recyclerview since the view was off screen
             *  Android would say the height of the view was zero.  So instead I added code that
             *  looked for the height of the top most view that was visible (and would therefore
             *  have a height.
             *
             *  see https://stackoverflow.com/questions/29463560/findfirstvisibleitempositions-doesnt-work-for-recycleview-android
             *  because as a staggeredGrid layout you have a special case first visible method
             *  findFirstVisibleItemPositions that returns an array of (notice the S on the end of
             *  the method name.
             */
            StaggeredGridLayoutManager layoutMngr = ((StaggeredGridLayoutManager) parent.getLayoutManager());
            int firstVisibleItemPosition = layoutMngr.findFirstVisibleItemPositions(null)[0];

            int topPos = 0;
            try {
                topPos = parent.getChildAt(firstVisibleItemPosition).getMeasuredHeight()/2;
            } catch (Exception e) {
                e.printStackTrace();
            }

            outRect.set(0, topPos, 0, 0);
        } else {
            outRect.set(0, 0, 0, 0);
        }

    }
}
