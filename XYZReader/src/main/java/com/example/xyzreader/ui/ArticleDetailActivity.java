package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import java.util.List;
import java.util.Map;

public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String ID = "item_id";

    private Cursor mCursor;
    private long mStartId;

    private long mSelectedItemId;
    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private int mAdapterCurrentPosition;
    private int mAdapterStartingPosition;
    private ArticleDetailFragment mCurrentDetailsFragment;
    private boolean mIsReturning;

    private final SharedElementCallback mCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            if (mIsReturning) {

                ImageView sharedElement = mCurrentDetailsFragment.getArticleImage();
                if (sharedElement == null) {
                    names.clear();
                    sharedElements.clear();
                } else if (mAdapterStartingPosition != mAdapterCurrentPosition) {
                    names.clear();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        names.add(sharedElement.getTransitionName());
                    }
                    sharedElements.clear();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        sharedElements.put(sharedElement.getTransitionName(), sharedElement);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            postponeEnterTransition();
            setEnterSharedElementCallback(mCallback);
        }

        mAdapterStartingPosition = getIntent().getIntExtra(ArticleListActivity.ADAPTER_POSITION_START, 0);

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
                mSelectedItemId = mStartId;
            }

            mAdapterCurrentPosition = mAdapterStartingPosition;
        } else {
            mStartId = savedInstanceState.getLong(ID);
            mAdapterCurrentPosition = savedInstanceState.getInt(ArticleListActivity.ADAPTER_POSITION_CURRENT);
        }

        setContentView(R.layout.activity_article_detail);

        getLoaderManager().initLoader(0, null, this);

        mPagerAdapter = new MyPagerAdapter(getFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(mAdapterCurrentPosition);
        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                    mAdapterCurrentPosition = position;
                }
            }
        });



    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        mPagerAdapter.notifyDataSetChanged();

        if (mStartId > 0) {
            mCursor.moveToFirst();
            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    final int position = mCursor.getPosition();
                    mPager.setCurrentItem(position, false);
                    break;
                }
                mCursor.moveToNext();
            }
            mStartId = 0;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(ID, mStartId);
        outState.putInt(ArticleListActivity.ADAPTER_POSITION_CURRENT, mAdapterCurrentPosition);
    }


    @Override
    public void finishAfterTransition() {
        mIsReturning = true;
        Intent intent = new Intent();
        intent.putExtra(ArticleListActivity.ADAPTER_POSITION_START, mAdapterStartingPosition);
        intent.putExtra(ArticleListActivity.ADAPTER_POSITION_CURRENT, mAdapterCurrentPosition);
        setResult(RESULT_OK, intent);
        super.finishAfterTransition();
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            mCurrentDetailsFragment = (ArticleDetailFragment) object;
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID), position, mAdapterStartingPosition);
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }
}