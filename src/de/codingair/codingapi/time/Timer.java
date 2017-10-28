package de.codingair.codingapi.time;



public class Timer {
	private double lastStoppedTime = 0;
	private long currentTime = 0;
	
	public void start(){
		this.currentTime = System.currentTimeMillis();
	}
	
	public void stop(){
		this.lastStoppedTime = (double) (System.currentTimeMillis() - this.currentTime) / 1000;
		int temp = (int) (this.lastStoppedTime * 1000);
		this.lastStoppedTime = (double) temp;
		this.lastStoppedTime /= 1000;
	}

	/**
	 * Time is in MiliSeconds
	 * @return
	 */
	public double getLastStoppedTime() {
		return lastStoppedTime;
	}
}
