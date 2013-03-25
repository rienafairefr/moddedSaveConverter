package com.rienafairefr.moddedSaveConverter.tasks;

import com.sk89q.mclauncher.Task;

public class CustomTask extends Task {
	protected volatile boolean stop=false;

	@Override
	public Boolean cancel() {
		stop=true;
		return null;
	}

	@Override
	protected void execute() throws ExecutionException {
		
	}
}
