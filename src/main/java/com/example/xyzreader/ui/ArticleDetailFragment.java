package com.example.xyzreader.ui;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.util.Utils;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";

    private static final int TRANSPARENT = 0x00;
    private static final float LUMINANCE_CUTOFF = 0.5f;

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;

    private ImageView mPhotoView;
    private boolean mIsCard = false;
    private CoordinatorLayout mCoordinatorLayout;
    private Toolbar mToolbar;
    private NestedScrollView mNestedScrollView;
    private FloatingActionButton mFAB;
    private boolean isFABDown = false;
    private int mBottomBuffer;

    private int mTransitionIndex;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        return newInstance(itemId, -1);
    }

    public static ArticleDetailFragment newInstance(long itemId, int transitionIndex) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        arguments.putInt(ArticleListActivity.TRANSITION_TAG, transitionIndex);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        mTransitionIndex = getArguments().getInt(ArticleListActivity.TRANSITION_TAG, -1);

        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
        setHasOptionsMenu(true);
    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        mPhotoView = (ImageView) mRootView.findViewById(R.id.photo);

        mCoordinatorLayout =
                (CoordinatorLayout) mRootView.findViewById(R.id.detail_coordinator_layout);
        mToolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);

        mBottomBuffer = Math.round(getResources().getDimension(R.dimen.detail_body_bottom_margin));

        mFAB = (FloatingActionButton) mRootView.findViewById(R.id.share_fab);
        mFAB.setVisibility(View.GONE);
        mNestedScrollView =
                (NestedScrollView) mRootView.findViewById(R.id.detail_nested_scroll_view);

        mNestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(
                    NestedScrollView v,
                    int scrollX,
                    int scrollY,
                    int oldScrollX,
                    int oldScrollY) {

                //The share FAB appears and disappears when approaching the bottom and leaving the
                //bottom of the screen
                int bottom = mNestedScrollView.getChildAt(0).getHeight() - mNestedScrollView.getHeight();
                bottom -= mBottomBuffer;
                if(scrollY >= bottom && isFABDown) {
                    fabUp();
                } else if(scrollY < bottom && !isFABDown) {
                    fabDown();
                }
            }
        });

        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        updateStatusBar();

        return mRootView;
    }
    @Override
    public void onResume() {
        super.onResume();

        //make sure the share FAB is off screen initially
        if(mFAB != null) {
            fabDown();
        }
    }

    private void updateStatusBar() {

        //If running SDK 21+ make the status bar transparent
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCoordinatorLayout.setFitsSystemWindows(false);
            getActivity().getWindow().setStatusBarColor(TRANSPARENT);
            int statusBarHeight = (int) Math.round(
                    Math.ceil(25.0 * getActivity().getResources().getDisplayMetrics().density));
            CollapsingToolbarLayout.LayoutParams layoutParams =
                    (CollapsingToolbarLayout.LayoutParams) mToolbar.getLayoutParams();
            layoutParams.height += statusBarHeight;
            mToolbar.requestLayout();
        }
    }

    public void fabUp() {
        mFAB.setVisibility(View.VISIBLE);
        mFAB.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
        isFABDown = false;
    }

    public void fabDown() {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mFAB.getLayoutParams();
        mFAB.animate()
                .translationY(mFAB.getHeight() + lp.bottomMargin)
                .setInterpolator(new LinearInterpolator())
                .start();
        isFABDown = true;
    }

    static float progress(float v, float min, float max) {
        return constrain((v - min) / (max - min), 0, 1);
    }

    static float constrain(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        TextView titleView = (TextView) mRootView.findViewById(R.id.article_title);
        TextView bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
        bylineView.setMovementMethod(new LinkMovementMethod());
        TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));

            //TODO: fix this. There is no need to use this messy HTML logic
            bylineView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>"));
            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));

            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {
                                int dominantColor = Utils.getDominantColorFromBitmap(bitmap);
                                int darkDominantColor = Utils.darkenColor(dominantColor, Utils.DARKEN_CHANGE);
                                if(Color.luminance(dominantColor) > LUMINANCE_CUTOFF) {
                                    dominantColor = darkDominantColor;
                                }
                                mPhotoView.setImageBitmap(imageContainer.getBitmap());
                                mRootView.findViewById(R.id.meta_bar)
                                        .setBackgroundColor(dominantColor);

                                GradientDrawable gd = new GradientDrawable(
                                        GradientDrawable.Orientation.TOP_BOTTOM,
                                        new int[] {darkDominantColor, TRANSPARENT});
                                gd.setCornerRadius(0);

                                mRootView.findViewById(R.id.photo_gradient).setBackground(gd);
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mPhotoView.setTransitionName(getString(R.string.transition_photo) + mTransitionIndex);
                scheduleStartPostponedTransition(mPhotoView);
            }
        } else {
            mRootView.setVisibility(View.GONE);
            titleView.setText("N/A");
            bylineView.setText("N/A" );
            bodyView.setText("N/A");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
        //ActivityCompat.startPostponedEnterTransition(getActivity());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void scheduleStartPostponedTransition(final View sharedElement) {
        sharedElement.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                        getActivity().startPostponedEnterTransition();
                        return true;
                    }
                });
    }
}
