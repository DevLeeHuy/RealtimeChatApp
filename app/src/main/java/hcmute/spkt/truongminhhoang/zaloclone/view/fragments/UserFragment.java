package hcmute.spkt.truongminhhoang.zaloclone.view.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Iterables;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.Objects;

import hcmute.spkt.truongminhhoang.zaloclone.services.model.Users;
import hcmute.spkt.truongminhhoang.zaloclone.view.adapters.UserFragmentAdapter;
import hcmute.spkt.truongminhhoang.zaloclone.viewModel.DatabaseViewModel;
import hcmute.spkt.truongminhhoang.zaloclone.R;
public class UserFragment extends Fragment {
    private Context context;
    private DatabaseViewModel databaseViewModel;
    private ArrayList<Users> mUser;
    private String currentUserId;
    private RecyclerView recyclerView;
    private UserFragmentAdapter userFragmentAdapter;
    EditText et_search;

    public UserFragment(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user, container, false);
        init(view);
        fetchingAllUserName();
        return view;
    }



    private void fetchingAllUserName() {
        databaseViewModel.fetchingUserDataCurrent();
        databaseViewModel.fetchUserCurrentData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(DataSnapshot dataSnapshot) {
                Users users = dataSnapshot.getValue(Users.class);
                assert users != null;
                currentUserId = users.getId();
            }
        });

        databaseViewModel.fetchUserByNameAll();
        databaseViewModel.fetchUserNames.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(DataSnapshot dataSnapshot) {
                if (et_search.getText().toString().equals("")) {
                    mUser.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Users user = snapshot.getValue(Users.class);

                        assert user != null;
                        if (!(user.getEmailId() == null)
                        ) {
                            if (!currentUserId.equals(user.getId())) {
                                mUser.add(user);

                            }
                        }
                        userFragmentAdapter = new UserFragmentAdapter(mUser, context, false);
                        recyclerView.setAdapter(userFragmentAdapter);

                    }

                }
            }
        });
    }

    private void init(View view) {
        databaseViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(Objects.requireNonNull(getActivity()).getApplication()))
                .get(DatabaseViewModel.class);

        recyclerView = view.findViewById(R.id.user_list_recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        mUser = new ArrayList<>();
        et_search = view.findViewById(R.id.et_search);
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (et_search.getText().toString().startsWith(" "))
                    et_search.setText("");
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void searchUsers(String searchText) {
        if (!(searchText.isEmpty() && searchText.equals(""))) {
            databaseViewModel.fetchSearchedUser(searchText);

            //Get search on Username & Phone number field
            databaseViewModel.fetchSearchUser.forEach(fetch -> {
                fetch.observe(this, new Observer<DataSnapshot>() {
                    @Override
                    public void onChanged(DataSnapshot dataSnapshot) {
                        if (Iterables.size(dataSnapshot.getChildren()) <= 0
                                && databaseViewModel.fetchSearchUser.indexOf(fetch) == databaseViewModel.fetchSearchUser.size() - 1
                        ) {
                            return;
                        }

                        mUser.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Users users = snapshot.getValue(Users.class);
                            assert users != null;
                            if (!users.getId().equals(currentUserId)) {
                                mUser.add(users);
                            }
                        }
                        userFragmentAdapter = new UserFragmentAdapter(mUser, context, false);
                        recyclerView.setAdapter(userFragmentAdapter);
                    }
                });
            });
        } else {
            fetchingAllUserName();
        }
    }
}