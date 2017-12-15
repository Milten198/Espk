package com.pgssoft.testwarez.util;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import com.pgssoft.testwarez.core.BaseNavigationDrawerActivity;

import java.util.Stack;

/**
 * Created by dpodolak on 13.07.16.
 */
public class ParentManager implements Application.ActivityLifecycleCallbacks{

    private Stack<Activity> activityStack = new Stack<>();
    private int drawerMenuId = -1;


    public Stack<Activity> getParentStack() {
        return activityStack;
    }

    public int getMenuId(){
        return drawerMenuId;
    }

    private  void addActivityToStack(Activity parents) {
        activityStack.push(parents);
    }

    private  void clearParent(){
        activityStack.clear();
    }

    public  void setMenuId(int menuId){
        drawerMenuId = menuId;
    }



    public void runParentActivity(Activity activity) {
        Stack<Activity> stack = getParentStack();

        if (stack.size() > 2) {

            Intent parentActivityIntent = new Intent(activity, stack.firstElement().getClass());
            parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            parentActivityIntent.putExtra(BaseNavigationDrawerActivity.CURRENT_MENU_ID, getMenuId());
            activity.startActivity(parentActivityIntent);

            if (activity instanceof FinishWithoutAnim){
                ((FinishWithoutAnim) activity).finishWithoutAnimation();
                return;
            }
        }

        activity.finish();
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (BaseNavigationDrawerActivity.class.isAssignableFrom(activity.getClass())){
            clearParent();
        }
        addActivityToStack(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (!BaseNavigationDrawerActivity.class.isAssignableFrom(activity.getClass())){
           activityStack.remove(activity);
        }
    }
}
