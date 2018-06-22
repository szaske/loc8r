package com.loc8r.seattle.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.loc8r.seattle.R;


public class StampListDecoration extends RecyclerView.ItemDecoration {
    private Drawable mDivider;
    private int vineHeight;
    private int halfVineWidth;

    public StampListDecoration(Drawable divider, Context context) {
        mDivider = divider;

        // We acquire vine height, so we can properly draw the vine to the top and bottom of
        // the vineLine.  Without it the draw would end up half way up the vineLine and look weird
        vineHeight = (int) context.getResources().getDimension(R.dimen.vineline_height)/2;
        halfVineWidth = mDivider.getIntrinsicWidth()/2;
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
    @Override public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(canvas, parent, state);

        // All layouts (left and right) are considered children
        int childCount = parent.getChildCount();

        for (int viewablePosition = 0; viewablePosition < childCount; viewablePosition++) {
            View child = parent.getChildAt(viewablePosition);

            int listPosition = parent.getChildAdapterPosition(child);

            // Initialize Bounds for the decorations
            int decoratorLeft;
            int decoratorRight;
            int decoratorTop = child.getTop();
            int decoratorBottom = child.getBottom();

            // Special case, if we're one of the first 2 items in the list
            // make it only half height starting at middle of view
            if(listPosition <= 1){
                decoratorTop = (int) Math.floor ((child.getTop() + child.getBottom())/2) - vineHeight; // To fix a minor graphical bug at top of "POI vine" in view
            }

            // Special case for last two item
            // we're subtracting 2 because list position starts at 0
            // and childcount at 1
            if(listPosition >= parent.getAdapter().getItemCount() - 2){
                decoratorBottom = (int) Math.floor ((child.getTop() + child.getBottom())/2) + vineHeight;
            }

            // split the line to draw half on left, half on right
            if(isOnLeft(viewablePosition)){
                decoratorLeft = child.getRight() - halfVineWidth;
                decoratorRight = child.getRight() + halfVineWidth;
            } else {
                decoratorLeft = child.getLeft() - halfVineWidth;
                decoratorRight = child.getLeft() + halfVineWidth;
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

        /*
           Special case.  Te first right side item in the list should have an extra 50% top
           offset so that these equal sized views are perfectly staggered.
         */
//        if (parent.getChildAdapterPosition(view) == 1) {
//
//            /**
//             *  We would normally do a outRect.top = view.getHeight()/2 to create a 50% top offset on the first right item in the list.
//             *  However, problems would arise if we paused the app when the top right item was scrolled off screen.
//             *  In this situation, when we re-inflated the recyclerview since the view was off screen
//             *  Android would say the height of the view was zero.  So instead I added code that
//             *  looked for the height of the top most view that was visible (and would therefore
//             *  have a height.
//             *
//             *  see https://stackoverflow.com/questions/29463560/findfirstvisibleitempositions-doesnt-work-for-recycleview-android
//             *  because as a staggeredGrid layout you have a special case first visible method
//             *  findFirstVisibleItemPositions that returns an array of (notice the S on the end of
//             *  the method name.
//             */
//            StaggeredGridLayoutManager layoutMngr = ((StaggeredGridLayoutManager) parent.getLayoutManager());
//            int firstVisibleItemPosition = layoutMngr.findFirstVisibleItemPositions(null)[0];
//
//            int topPos = 0;
//            try {
//                topPos = parent.getChildAt(firstVisibleItemPosition).getMeasuredHeight()/2;
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            outRect.set(0, topPos, 0, 0);
//        } else {
//            outRect.set(0, 0, 0, 0);
//        }

    }
}
