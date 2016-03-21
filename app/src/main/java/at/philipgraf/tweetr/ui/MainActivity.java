package at.philipgraf.tweetr.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.twitter.sdk.android.core.TwitterException;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import at.philipgraf.tweetr.R;
import at.philipgraf.tweetr.adapters.TweetAdapter;
import at.philipgraf.tweetr.data.Tweet;
import at.philipgraf.tweetr.data.TwitterHelper;
import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TWEETR/MainActivity";
    private TwitterHelper mTwitterHelper;
    private TweetAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Realm mRealm;

    @Bind(R.id.relativeLayout) RelativeLayout mRelativeLayout;
    @Bind(R.id.recyclerView) RecyclerView mRecyclerView;
    @Bind(R.id.swipeRefreshLayout) SwipeRefreshLayout mSwipeLayout;
    @Bind(R.id.newTweetsLabel) TextView mNewTweetsLabel;
    @Bind(R.id.progressBar) ProgressBar mProgressBar;
    @Bind(R.id.searchResultsFromTwitter) TextView mSearchWebResultsLabel;
    private int mNumberOfUnvisitedNewItems;
    private Snackbar mRateLimitSnackbar;
    private boolean mIsSearching = false;

    // InstanceState

    private static final String RECYCLER_VIEW_STATE_KEY = "RECYCLER_VIEW_STATE";
    private static final String RECYCLER_VIEW_TWEETS_KEY = "RECYCLER_VIEW_TWEETS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mRealm = Realm.getInstance(this);

        mTwitterHelper = new TwitterHelper(this, mRelativeLayout);

        // Set RefreshListener

        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
            }
        });

        // Make ProgressSpinner visible

        mProgressBar.setVisibility(View.VISIBLE);
        mSwipeLayout.setVisibility(View.INVISIBLE);

        // Restore savedInstanceState

        if(savedInstanceState != null){
            Parcelable recyclerViewState = savedInstanceState.getParcelable(RECYCLER_VIEW_STATE_KEY);
            ArrayList<Tweet> recyclerViewTweets = savedInstanceState.getParcelableArrayList(RECYCLER_VIEW_TWEETS_KEY);

            updateData(recyclerViewTweets, null);
            mLayoutManager.onRestoreInstanceState(recyclerViewState);
        } else {
            getData(); // Initialize HomeTimeLine RecyclerView if it hasn't by onRestoreInstanceState
        }

        // Update Data Every Minute

        mNumberOfUnvisitedNewItems = 0;

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService
                .scheduleAtFixedRate(
                        new Runnable() {
                            public void run() {
                                if(!mIsSearching) {
                                    getData(false, new TwitterHelper.UpdateItemsCallback() {
                                        @Override
                                        public void itemsChanged(final int numberOfNewItems) {

                                            mNumberOfUnvisitedNewItems += numberOfNewItems;

                                            // Assign Text to Button
                                            if (mNumberOfUnvisitedNewItems == 1) {
                                                mNewTweetsLabel.setText("1 neuer Tweet");
                                            } else {
                                                mNewTweetsLabel.setText(mNumberOfUnvisitedNewItems + " neue Tweets");
                                            }

                                            // When User clicks on the Button, scroll up and hide it
                                            mNewTweetsLabel.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    mLayoutManager.smoothScrollToPosition(mRecyclerView, null, 0);
                                                    mNewTweetsLabel.setVisibility(View.INVISIBLE);
                                                }
                                            });

                                            // If User has reached one of the new items, hide the button
                                            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                                @Override
                                                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                                    super.onScrollStateChanged(recyclerView, newState);

                                                    if (((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition() <= mNumberOfUnvisitedNewItems - 1) { // Determines the position of first visible item
                                                        mNewTweetsLabel.setVisibility(View.INVISIBLE);
                                                        mNumberOfUnvisitedNewItems = 0;
                                                    }
                                                }
                                            });

                                            // Make Button visible
                                            mNewTweetsLabel.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }

                                Log.d(TAG, "AutoRefresh of HomeTimeline called");
                            }
                        }, 1, 1, TimeUnit.MINUTES
                );
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(mLayoutManager != null) {
            outState.putParcelable(RECYCLER_VIEW_STATE_KEY, mLayoutManager.onSaveInstanceState());
            outState.putParcelableArrayList(RECYCLER_VIEW_TWEETS_KEY, mAdapter.getTweets());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If Tweet was changed in TweetActivity, this is responsible for updating the RecyclerView

        super.onActivityResult(requestCode, resultCode, data);
        Tweet originalTweet;

        if((originalTweet = data.getParcelableExtra(TweetActivity.ORIGINAL_TWEET_KEY)) != null) {
            mAdapter.swap(originalTweet, (Tweet) data.getParcelableExtra(TweetActivity.RESULT_KEY));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        // Retrieve the SearchView and plug it into SearchManager
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        //SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        //searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                mTwitterHelper.searchTwitter(query, new TwitterHelper.HomeTimelineCallback() {
                    @Override
                    public void onHomeTimelineLoaded(ArrayList<Tweet> homeTimeline) {
                        mSearchWebResultsLabel.setVisibility(View.VISIBLE);
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mSwipeLayout.getLayoutParams();
                        params.addRule(RelativeLayout.BELOW, R.id.searchResultsFromTwitter);

                        if (mAdapter != null) {
                            mAdapter.replace(homeTimeline);
                        }
                    }

                    @Override
                    public void onError(TwitterException e) {
                        Log.e(TAG, "Error while searching Twitter. Stacktrace: ");
                        e.printStackTrace();
                    }

                    @Override
                    public void onNetworkUnavailable() {
                        Snackbar.make(mRelativeLayout, "Keine Internetverbindung", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Online gehen", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        onQueryTextSubmit(query);
                                    }
                                })
                                .show();
                    }

                    @Override
                    public void onAPIRateLimit() {
                        mRateLimitSnackbar = Snackbar.make(mRelativeLayout, "Das Limit für Twitter-Sucher ist erreicht. Bitte versuche es in ein paar Minuten noch einmal", Snackbar.LENGTH_SHORT);

                        mRateLimitSnackbar.setAction("Erneut versuchen", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onQueryTextSubmit(query);
                            }
                        })
                                .show();
                    }
                });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.equals("")) {
                    mIsSearching = true;

                    if (mAdapter != null) {
                        mAdapter.replace(mTwitterHelper.searchRealm(newText));
                    }
                }
                return false;
            }
        });


            MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search), new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    mIsSearching = false;

                    mSearchWebResultsLabel.setVisibility(View.INVISIBLE);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mSwipeLayout.getLayoutParams();
                    params.addRule(RelativeLayout.BELOW, -1);

                    if (mAdapter != null) {
                        mAdapter.replace(mTwitterHelper.discardSearch());
                    }
                    return true;
                }
            });

        return true;
    }

    private void getData() {
        getData(true, null);
    }

    private void getData(final boolean showRateLimitWarning, final TwitterHelper.UpdateItemsCallback callback) {
        mTwitterHelper.updateHomeTimeline(new TwitterHelper.HomeTimelineCallback() {
            @Override
            public void onHomeTimelineLoaded(ArrayList<Tweet> data) {
                Log.d(TAG, "HomeTimeLine Data loaded");
                updateData(data, callback);
            }

            @Override
            public void onError(TwitterException e) {
                Log.e(TAG, "Error while loading HomeTimeLine Data. Stacktrace: ");
                e.printStackTrace();
                mSwipeLayout.setRefreshing(false);

                // REMOVE WHEN IMPLEMENTING REALM
                mSwipeLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAPIRateLimit() {
                mSwipeLayout.setRefreshing(false);

                if (showRateLimitWarning) {
                    mRateLimitSnackbar = Snackbar.make(mRelativeLayout, "Das Limit für Twitter-Anfragen ist erreicht. Bitte versuche es in ein paar Minuten noch einmal", Snackbar.LENGTH_INDEFINITE);

                    mRateLimitSnackbar.setAction("Erneut versuchen", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getData(true, callback);
                        }
                    })
                            .show();
                }
            }

            @Override
            public void onNetworkUnavailable() {
                mSwipeLayout.setRefreshing(false);
                Snackbar.make(mRelativeLayout, "Keine Internetverbindung", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Online gehen", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getData(showRateLimitWarning, callback);
                            }
                        })
                        .show();
            }
        }, showRateLimitWarning);
    }

    private void updateData(ArrayList<Tweet> homeTimeLine, TwitterHelper.UpdateItemsCallback callback) {

        if(mAdapter == null) {
            mAdapter = new TweetAdapter(homeTimeLine, mTwitterHelper);
            mRecyclerView.setAdapter(mAdapter);

            mLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(mLayoutManager);
        } else {
            mAdapter.swap(homeTimeLine, callback);
        }

        mSwipeLayout.setRefreshing(false);

        if(mRateLimitSnackbar != null) mRateLimitSnackbar.dismiss();

        mProgressBar.setVisibility(View.GONE);
        mSwipeLayout.setVisibility(View.VISIBLE);
    }
}
