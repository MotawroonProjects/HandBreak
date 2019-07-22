package com.creativeshare.hand_break.activities_fragments.home_activity.fragments;

import android.app.ProgressDialog;
import android.graphics.PorterDuff;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.creativeshare.hand_break.R;
import com.creativeshare.hand_break.activities_fragments.home_activity.activity.HomeActivity;
import com.creativeshare.hand_break.adapters.Adversiment_Adapter;
import com.creativeshare.hand_break.adapters.CatogriesAdapter;
import com.creativeshare.hand_break.models.CityModel;
import com.creativeshare.hand_break.adapters.Spinner_Adapter;
import com.creativeshare.hand_break.adapters.Spinner_Sub_catogry_Adapter;
import com.creativeshare.hand_break.models.Catogry_Model;
import com.creativeshare.hand_break.models.UserModel;
import com.creativeshare.hand_break.preferences.Preferences;
import com.creativeshare.hand_break.remote.Api;
import com.creativeshare.hand_break.share.Common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Fragment_Main extends Fragment {
    private HomeActivity homeActivity;
    private String cuurent_language;
    private List<Catogry_Model.Categories.sub> subs;
    private Spinner_Sub_catogry_Adapter spinner_sub_catogry_adapter;
    private Spinner sub_cat, cities;
    private Spinner_Adapter city_adapter;
    private List<CityModel> cities_models;
    private RecyclerView rec_search;
    private ImageView im_search;
    private Adversiment_Adapter adversiment_adapter;
    private List<Catogry_Model.Categories> categories;
    private List<Catogry_Model.Advertsing> advertsings;
    private Preferences preferences;
    private UserModel userModel;
    private String maincatogryfk;
    private ProgressBar progBar;
    private LinearLayout ll_no_order;
    private boolean isLoading = false;
    private int current_page = 1;
    private GridLayoutManager manager;
    private String user_id = "all";
    private String city_id = "all";
    private String sub_id = "all";
    private int total_page;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        initView(view);
        getCities();
        //     getadversment();
        return view;
    }

    private void initView(View view) {
        sub_cat = view.findViewById(R.id.sub_cat);
        cities = view.findViewById(R.id.sp_city);
        rec_search = view.findViewById(R.id.rec_search);
        progBar = view.findViewById(R.id.progBar);
        im_search = view.findViewById(R.id.im_search);
        ll_no_order = view.findViewById(R.id.ll_no_order);
        subs = new ArrayList<>();
        categories = new ArrayList<>();
        advertsings = new ArrayList<>();

        homeActivity = (HomeActivity) getActivity();
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(homeActivity);
        if (userModel == null) {
            user_id = "";
        } else {
            user_id = userModel.getUser_id();
        }

        progBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(homeActivity, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);

        Paper.init(homeActivity);
        cuurent_language = Paper.book().read("lang", Locale.getDefault().getLanguage());
        cities_models = new ArrayList<>();
        if (cuurent_language.equals("ar")) {
            cities_models.add(new CityModel("إختر"));
            subs.add(new Catogry_Model.Categories.sub("الكل"));

        } else {
            cities_models.add(new CityModel("Choose"));
            subs.add(new Catogry_Model.Categories.sub("all"));

        }

        spinner_sub_catogry_adapter = new Spinner_Sub_catogry_Adapter(homeActivity, subs);
        sub_cat.setAdapter(spinner_sub_catogry_adapter);
        city_adapter = new Spinner_Adapter(homeActivity, cities_models);
        cities.setAdapter(city_adapter);
        adversiment_adapter = new Adversiment_Adapter(advertsings, categories, homeActivity);
        rec_search.setDrawingCacheEnabled(true);
        rec_search.setItemViewCacheSize(25);
        rec_search.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        manager = new GridLayoutManager(homeActivity, 1);
        rec_search.setLayoutManager(manager);
        rec_search.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    int total_item = manager.getItemCount();
                    int last_item_pos = manager.findLastCompletelyVisibleItemPosition();
                    Log.e("msg", total_item + "  " + last_item_pos);
                    Log.e("msg", current_page+"");

                    if (last_item_pos >= (total_item - 5) && !isLoading&&total_page>current_page) {
                        isLoading = true;
                        advertsings.add(null);
                        adversiment_adapter.notifyItemInserted(advertsings.size() - 1);
                        int page = current_page + 1;
                        //cuurent_language+=1;
                        Log.e("msg", page+"");

                        loadMore(page);
                    }
                }
            }

        });
        rec_search.setAdapter(adversiment_adapter);

        sub_cat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    sub_id = "all";

                } else {
                    sub_id = subs.get(i).getSub_category_fk();
                    getadversment();

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        cities.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {


            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    city_id = "all";
                } else {
                    city_id = cities_models.get(i).getId_city();
                    getadversment();

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });

        im_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getadversment();
            }
        });
    }

    public static Fragment_Main newInstance() {
        return new Fragment_Main();
    }

    public void addsubtosppinner(List<Catogry_Model.Categories.sub> subs, String main_category_fk) {

        this.subs.clear();
        if (cuurent_language.equals("ar")) {
            this.subs.add(new Catogry_Model.Categories.sub("الكل"));

        } else {
            this.subs.add(new Catogry_Model.Categories.sub("all"));

        }
        this.subs.addAll(subs);
        spinner_sub_catogry_adapter.notifyDataSetChanged();
        sub_cat.setSelection(0);

        maincatogryfk = main_category_fk;


        getadversment();
    }

    private void getCities() {

        final ProgressDialog dialog = Common.createProgressDialog(homeActivity, getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.show();

        Api.getService()
                .getCities(cuurent_language)
                .enqueue(new Callback<List<CityModel>>() {
                    @Override
                    public void onResponse(Call<List<CityModel>> call, Response<List<CityModel>> response) {
                        dialog.dismiss();

                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                cities_models.clear();
                                if (cuurent_language.equals("ar")) {
                                    cities_models.add(new CityModel("إختر"));
                                } else {
                                    cities_models.add(new CityModel("Choose"));

                                }
                                cities_models.addAll(response.body());
                                city_adapter.notifyDataSetChanged();
                            }
                        } else {
                            try {
                                Toast.makeText(homeActivity, R.string.failed, Toast.LENGTH_SHORT).show();
                                Log.e("Error_code", response.code() + "" + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<CityModel>> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            Toast.makeText(homeActivity, R.string.something, Toast.LENGTH_SHORT).show();
                            Log.e("Error", t.getMessage());
                        } catch (Exception e) {

                        }
                    }
                });

    }

    private void getadversment() {
        rec_search.setVisibility(View.GONE);
        ll_no_order.setVisibility(View.GONE);
        progBar.setVisibility(View.VISIBLE);


        Api.getService()
                .getadversment(1, user_id, maincatogryfk, sub_id, city_id)
                .enqueue(new Callback<Catogry_Model>() {
                    @Override
                    public void onResponse(Call<Catogry_Model> call, Response<Catogry_Model> response) {
                        progBar.setVisibility(View.GONE);
                        rec_search.setVisibility(View.VISIBLE);
                        if (response.isSuccessful() && response.body() != null && response.body().getAdvertsing() != null) {
                            advertsings.clear();
                            advertsings.addAll(response.body().getAdvertsing());
                            categories.clear();
                            categories.addAll(response.body().getCategories());
                            //total_page = response.body().getMeta().getLast_page();
                            if (advertsings.size() > 0) {
                                //ll_no_order.setVisibility(View.GONE);
                                adversiment_adapter.notifyDataSetChanged();
                                total_page = response.body().getMeta().getLast_page();
                            } else {
                                ll_no_order.setVisibility(View.VISIBLE);

                            }
                        } else {

                            Toast.makeText(homeActivity, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            try {
                                Log.e("Error_code", response.code() + "_" + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Catogry_Model> call, Throwable t) {
                        try {

                            progBar.setVisibility(View.GONE);
                            Toast.makeText(homeActivity, getString(R.string.something), Toast.LENGTH_SHORT).show();
                            Log.e("error", t.getMessage());
                        } catch (Exception e) {
                        }
                    }
                });
    }

    private void loadMore(int page) {


        Api.getService()
                .getadversment(page, user_id, maincatogryfk, sub_id, city_id + "")
                .enqueue(new Callback<Catogry_Model>() {
                    @Override
                    public void onResponse(Call<Catogry_Model> call, Response<Catogry_Model> response) {
                        advertsings.remove(advertsings.size() - 1);
                        adversiment_adapter.notifyItemRemoved(advertsings.size() - 1);
                        isLoading = false;
                        if (response.isSuccessful() && response.body() != null && response.body().getAdvertsing() != null) {

                            advertsings.addAll(response.body().getAdvertsing());
                            categories.addAll(response.body().getCategories());

                            adversiment_adapter.notifyDataSetChanged();

                            current_page = response.body().getMeta().getCurrent_page();

                        } else {
                            Toast.makeText(homeActivity, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            try {
                                Log.e("Error_code", response.code() + "_" + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Catogry_Model> call, Throwable t) {
                        try {
                            isLoading = false;
                            advertsings.remove(advertsings.size() - 1);
                            adversiment_adapter.notifyItemRemoved(advertsings.size() - 1);
                           // isLoading = false;
                            Toast.makeText(homeActivity, getString(R.string.something), Toast.LENGTH_SHORT).show();
                            Log.e("error", t.getMessage());
                        } catch (Exception e) {
                        }
                    }
                });
    }
}
