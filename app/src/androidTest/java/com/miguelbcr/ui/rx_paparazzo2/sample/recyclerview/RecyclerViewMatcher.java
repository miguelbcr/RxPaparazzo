package com.miguelbcr.ui.rx_paparazzo2.sample.recyclerview;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class RecyclerViewMatcher {
    private final int recyclerViewId;

    public RecyclerViewMatcher(int recyclerViewId) {
        this.recyclerViewId = recyclerViewId;
    }

    public Matcher<View> atPosition(final int position) {
        return atPositionOnView(position, -1);
    }

    public Matcher<View> isEmpty() {
        return new TypeSafeMatcher<View>() {
            Resources resources = null;

            public void describeTo(Description description) {
                String idDescription = getResourceName(resources, recyclerViewId);
                description.appendText("Empty recycler view with id: " + idDescription);
            }

            public boolean matchesSafely(View view) {
                this.resources = view.getResources();

                RecyclerView recyclerView = (RecyclerView) view.getRootView().findViewById(recyclerViewId);
                if (!(recyclerView != null && recyclerView.getId() == recyclerViewId)) {
                    Log.i("MATCHER", "Recycler view missing");

                    return false;
                }

                int childCount = recyclerView.getAdapter().getItemCount();
                if (childCount > 0) {
                    Log.i("MATCHER", "Recycler view only has " + childCount + " items, expected it was empty");

                    return false;
                }

                return true;
            }
        };
    }

    private String getResourceName(Resources resources, int id) {
        if (resources == null) {
            return String.valueOf(id);
        }
        try {
            return resources.getResourceName(id);
        } catch (Resources.NotFoundException var4) {
            return String.format("%s (resource name not found)", id);
        }
    }

    public Matcher<View> atPositionOnView(final int position, final int targetViewId) {

        return new TypeSafeMatcher<View>() {
            Resources resources = null;

            public void describeTo(Description description) {
                if (targetViewId != -1) {
                    String targetDescription = getResourceName(resources, targetViewId);
                    description.appendText("child with id: " + targetDescription + " of ");
                }

                String idDescription = getResourceName(resources, recyclerViewId);
                description.appendText("item at index #" + position + " in view with id: " + idDescription);
            }

            public boolean matchesSafely(View view) {

                this.resources = view.getResources();

                RecyclerView recyclerView = (RecyclerView) view.getRootView().findViewById(recyclerViewId);
                if (!(recyclerView != null && recyclerView.getId() == recyclerViewId)) {
                    Log.i("MATCHER", "Recycler view missing");

                    return false;
                }

                int childCount = recyclerView.getAdapter().getItemCount();
                if (position >= childCount) {
                    Log.i("MATCHER", "Recycler view only has " + childCount + " items, cannot get index " + position);

                    return false;
                }

                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                if (viewHolder.itemView == null) {
                    Log.i("MATCHER", "Item view in view holder was null");

                    return false;
                }

                if (targetViewId == -1) {
                    boolean isItemView = (view.equals(viewHolder.itemView));

                    View targetView = viewHolder.itemView;
                    Log.i("MATCHER", targetView.toString() + " == " + view + " = " + isItemView);

                    return isItemView;
                }

                View targetView = viewHolder.itemView.findViewById(targetViewId);
                boolean isItemView = view.equals(targetView);

                Log.i("MATCHER", targetView.toString() + " == " + view + " = " + isItemView);

                return isItemView;
            }
        };
    }
}
