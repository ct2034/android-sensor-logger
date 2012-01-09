package at.feichtinger.sensorlogger.ui.layout;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.LinearLayout;

/**
 * A custom layout class extending the LinearLayout. This layout will delegate
 * calls to Checkable to a check box inside the LinearLayout, thus making it
 * possible to have a list of checkable items.
 */
public class CheckableLinearLayout extends LinearLayout implements Checkable {
	private boolean checked;
	private List<Checkable> checkableViews = new ArrayList<Checkable>();

	public CheckableLinearLayout(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		for (int index = 0; index < getChildCount(); index++) {
			final View v = getChildAt(index);
			if (v instanceof Checkable) {
				checkableViews.add((Checkable) v);
			}
		}
	}

	@Override
	public boolean isChecked() {
		return checked;
	}

	@Override
	public void setChecked(final boolean checked) {
		this.checked = checked;

		for (final Checkable c : checkableViews) {
			c.setChecked(checked);
		}
	}

	@Override
	public void toggle() {
		checked = checked ? false : true;

		for (final Checkable c : checkableViews) {
			c.toggle();
		}
	}

}
