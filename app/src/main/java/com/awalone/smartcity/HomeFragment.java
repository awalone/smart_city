package com.awalone.smartcity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;



public class HomeFragment extends Fragment {

    public static TabLayout tabLayout;
    public static ViewPager viewPager;
    public static int int_items = 3;

    String Lapor, LaporAll, LaporMap;


    private int[] tabIcons = {
//            R.drawable.diagnosa,
//            R.drawable.penyakit,
//            R.drawable.perawatan
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /**
         *Inflate beranda_layout and setup Views.
         */
        View x = inflater.inflate(R.layout.home_layout, null);
        tabLayout = (TabLayout) x.findViewById(R.id.tabs);
        viewPager = (ViewPager) x.findViewById(R.id.viewpager);

        View main = inflater.inflate(R.layout.activity_main, null);


        Lapor = "Laporan";
        LaporAll = "Semua Laporan";
        LaporMap = "Peta Laporan";
        /**
         *Set an Apater for the View Pager
         */
        viewPager.setAdapter(new MyAdapter(getChildFragmentManager()));

        /**
         * Now , this is a workaround ,
         * The setupWithViewPager dose't works without the runnable .
         * Maybe a Support Library Bug .
         */

        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
                setupTabIcons();
            }
        });

        return x;

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onActivityResult(requestCode, resultCode, data);


                Log.i("in activity result", "");
            }
        }
    }

    private void setupTabIcons() {
//        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
//        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
//        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
    }

    class MyAdapter extends FragmentStatePagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Return fragment with respect to Position .
         */

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new Laporan_Fragment();
                case 1:
                    return new Laporan_all_Fragment();
                case 2:
                    return new Laporan_map_Fragment();
            }
            return null;
        }

        @Override
        public int getCount() {

            return int_items;

        }

        /**
         * This method returns the title of the tab according to the position.
         * <p/>
         * private int[] imageResId = {
         * R.drawable.,
         * R.drawable.ic_tab_weather,
         * R.drawable.ic_tab_calendar
         * };
         */

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
//                    getActivity().setTitle(R.string.lapor);
                    return Lapor;
                case 1:
//                    getActivity().setTitle(R.string.lapor_all);
                    return LaporAll;
                case 2:
//                    getActivity().setTitle(R.string.lapor_map);
                    return LaporMap;
            }
            return null;
        }
    }
}
