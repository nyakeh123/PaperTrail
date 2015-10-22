package uk.co.nyakeh.papertrail;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.util.UUID;

public class BookFragment extends Fragment {
    private static final String ARG_BOOK_ID = "book_id";

    private ViewPager mViewPager;
    private Book mBook;
    Switch mSwitch_status;

    public static BookFragment newInstance(UUID bookId) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(ARG_BOOK_ID, bookId);

        BookFragment fragment = new BookFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        UUID bookId = (UUID) getArguments().getSerializable(ARG_BOOK_ID);
        mBook = BookLab.get(getActivity()).getBook(bookId);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(mBook.getTitle());
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book, container, false);

        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        if (mViewPager != null) {
            setupViewPager((ViewPager) view.findViewById(R.id.viewpager));
        }
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(mViewPager);
        mViewPager.setTag(R.string.book, mBook);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_book, menu);

        MenuItem item = menu.findItem(R.id.status_switch_item);
        item.setActionView(R.layout.switch_layout);
        mSwitch_status = (Switch) item.getActionView().findViewById(R.id.status_switch);
        if (mBook.getStatus().equals(Constants.READING)) {
            mSwitch_status.setChecked(true);
        }
        mSwitch_status.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mBook.setStatus(Constants.READING);
                } else {
                    if (mBook.isFinished()) {
                        mBook.setStatus(Constants.ARCHIVE);
                    } else {
                        mBook.setStatus(Constants.QUEUE);
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete_book:
                BookLab.get(getActivity()).deleteBook(mBook.getId());
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        BookLab.get(getActivity()).updateBook(mBook);
    }

    private void setupViewPager(ViewPager viewPager) {
        PageAdapter adapter = new PageAdapter(getActivity().getSupportFragmentManager());
        adapter.addFragment(new ProgressFragment(), "Progress");
        adapter.addFragment(new MetaDataFragment(), "Meta");
        viewPager.setAdapter(adapter);
    }
}
