package ru.sstu.vak.periscopeclient.viewPager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import java.util.ArrayList;
import ru.sstu.vak.periscopeclient.viewPager.fragments.BroadcastsFragment;
import ru.sstu.vak.periscopeclient.viewPager.fragments.HomeFragment;
import ru.sstu.vak.periscopeclient.viewPager.fragments.MapFragment;

/**
 * Created by Anton on 24.04.2018.
 */

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
    static final int PAGE_COUNT = 2;

    private ArrayList<Fragment> fragmentList;

//    @Override
//    public int getItemPosition(Object object) {
//        return POSITION_NONE;
//    }

    public ArrayList<Fragment> getFragmentsList() {
        return fragmentList;
    }

    public MyFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
//            case 0: {
//                return "Главная";
//            }
            case 0: {
                return "Список";
            }
            default: {
                return "Карта";
            }
        }
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        switch (position) {
//            case 0: {
//                fragment = (Fragment) HomeFragment.newInstance(position);
//                break;
//            }
            case 0: {
                fragment = (Fragment) BroadcastsFragment.newInstance(position);
                break;
            }
            case 1: {
                fragment = (Fragment) MapFragment.newInstance(position);
                break;
            }
            default: {
                fragment = null; // if it's execute, need add page in switch
            }
        }
        if (fragmentList == null) {
            fragmentList = new ArrayList<Fragment>();
        }
        fragmentList.add(fragment);
        return fragment;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
}