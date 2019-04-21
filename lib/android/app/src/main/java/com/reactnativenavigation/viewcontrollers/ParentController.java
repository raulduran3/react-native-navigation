package com.reactnativenavigation.viewcontrollers;

import android.app.Activity;
import android.support.annotation.CallSuper;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.reactnativenavigation.parse.Options;
import com.reactnativenavigation.parse.params.Bool;
import com.reactnativenavigation.presentation.Presenter;
import com.reactnativenavigation.utils.CollectionUtils;
import com.reactnativenavigation.views.Component;

import java.util.Collection;

import static com.reactnativenavigation.utils.CollectionUtils.*;

public abstract class ParentController<T extends ViewGroup> extends ChildController {

	public ParentController(Activity activity, ChildControllersRegistry childRegistry, String id, Presenter presenter, Options initialOptions) {
		super(activity, childRegistry, id, presenter, initialOptions);
	}

    @Override
    public void setWaitForRender(Bool waitForRender) {
        super.setWaitForRender(waitForRender);
        applyOnController(getCurrentChild(), controller -> ((ViewController) controller).setWaitForRender(waitForRender));
    }

    @Override
    public void setDefaultOptions(Options defaultOptions) {
	    super.setDefaultOptions(defaultOptions);
	    forEach(getChildControllers(), (child) -> child.setDefaultOptions(defaultOptions));
    }

    @Override
    @CheckResult
    public Options resolveCurrentOptions() {
	    if (CollectionUtils.isNullOrEmpty(getChildControllers())) return initialOptions;
        return getCurrentChild()
                .resolveCurrentOptions()
                .copy()
//                .withDefaultOptions(initialOptions);
                .withInitialOptions(initialOptions);
    }


    public Options resolveChildOptions(ViewController child) {
	    if (child == this) return resolveCurrentOptions();
        return child
                .resolveCurrentOptions()
                .copy()
                .withInitialOptions(initialOptions);
    }

    @Override
    @CheckResult
    public Options resolveCurrentOptions(Options defaultOptions) {
        return resolveCurrentOptions().withDefaultOptions(defaultOptions);
    }

    protected abstract ViewController getCurrentChild();

    @NonNull
	@Override
	public T getView() {
		return (T) super.getView();
	}

	@NonNull
	@Override
	protected abstract T createView();

    @NonNull
	public abstract Collection<? extends ViewController> getChildControllers();

	@Nullable
	@Override
	public ViewController findController(final String id) {
		ViewController fromSuper = super.findController(id);
		if (fromSuper != null) return fromSuper;

		for (ViewController child : getChildControllers()) {
			ViewController fromChild = child.findController(id);
			if (fromChild != null) return fromChild;
		}

		return null;
	}

    @Nullable
    @Override
    public ViewController findController(View child) {
        ViewController fromSuper = super.findController(child);
        if (fromSuper != null) return fromSuper;

        for (ViewController childController : getChildControllers()) {
            ViewController fromChild = childController.findController(child);
            if (fromChild != null) return fromChild;
        }

        return null;
    }

    @Override
    public boolean containsComponent(Component component) {
        if (super.containsComponent(component)) {
            return true;
        }
        for (ViewController child : getChildControllers()) {
            if (child.containsComponent(component)) return true;
        }
        return false;
    }

    @CallSuper
    public void applyChildOptions(Options options, Component child) {
        this.options = initialOptions.mergeWith(options);
    }

    @CallSuper
    public void mergeChildOptions(Options options, ViewController childController, Component child) {

    }

	@Override
	public void destroy() {
		super.destroy();
		for (ViewController child : getChildControllers()) {
			child.destroy();
		}
	}

	@CallSuper
    protected void clearOptions() {
	    performOnParentController(parent -> ((ParentController) parent).clearOptions());
        options = initialOptions.copy().clearOneTimeOptions();
    }

    public void setupTopTabsWithViewPager(ViewPager viewPager) {

    }

    public void clearTopTabs() {

    }

    @Override
    public boolean isRendered() {
        return getCurrentChild() != null && getCurrentChild().isRendered();
    }

    public void onChildDestroyed(Component child) {

    }

    public int getTopInset(ViewController child) {
        if (isRoot()) {
            return resolveChildOptions(child).statusBar.drawBehind.isTrue() ? 0 : 63;
        }
        return super.getTopInset(child);
    }

    @Override
    public boolean applyTopInsets() {
        return getCurrentChild().applyTopInsets();
    }

    @Override
    public boolean onMeasureChild(CoordinatorLayout parent, ViewGroup child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        ViewController controller = findController(child);
        if (controller == null) return super.onMeasureChild(parent, child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
        return onMeasureChild(parent, controller, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
    }

    private boolean onMeasureChild(CoordinatorLayout parent, ViewController child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        if (!(child instanceof ParentController)) {
            int height = View.MeasureSpec.getSize(parentHeightMeasureSpec);
            height -= child.getBottomInsets();
            int spec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
            parent.onMeasureChild(child.getView(), parentWidthMeasureSpec, widthUsed, spec, heightUsed);
            return true;
        }
        return false;
    }
}
