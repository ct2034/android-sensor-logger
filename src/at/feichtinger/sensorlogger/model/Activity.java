package at.feichtinger.sensorlogger.model;

import at.feichtinger.sensorlogger.R;

public enum Activity {
	WALK(R.string.activity_walk, R.drawable.activity_walk), RUN(R.string.activity_run, R.drawable.activity_run), STAND(
			R.string.activity_stand, R.drawable.activity_stand), STAIRSUP(R.string.activity_stairsup,
			R.drawable.activity_stairsup), STAIRSDOWN(R.string.activity_stairsdown, R.drawable.activity_stairsdown), CYCLE(
			R.string.activity_cycle, R.drawable.activity_cycle), DRIVING(R.string.activity_drivecar,
			R.drawable.activity_drivecar);

	private final int stringResourceKey;
	private final int drawableResourceKey;

	private Activity(int stringKey, int drawableResourceKey) {
		this.stringResourceKey = stringKey;
		this.drawableResourceKey = drawableResourceKey;
	}

	public int getStringResourceKey() {
		return stringResourceKey;
	}

	public int getDrawableResourceKey() {
		return drawableResourceKey;
	}
}
