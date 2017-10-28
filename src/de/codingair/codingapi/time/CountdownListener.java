package de.codingair.codingapi.time;

public abstract class CountdownListener {
	protected abstract void CountdownStartEvent();
	protected abstract void CountdownEndEvent();
	protected abstract void CountdownCancelEvent();
	protected abstract void onTick(int timeLeft);
}
