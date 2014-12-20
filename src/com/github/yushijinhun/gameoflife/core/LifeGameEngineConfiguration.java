package com.github.yushijinhun.gameoflife.core;

public final class LifeGameEngineConfiguration {

	public static final int DEFAULT_THREADS = Runtime.getRuntime()
			.availableProcessors();

	public final int threads;
	public final int width;
	public final int height;

	public LifeGameEngineConfiguration(int width, int height, int threads) {
		if ((threads < 1) || (width < 1) || (height < 1)) {
			throw new IllegalArgumentException();
		}

		this.threads = threads;
		this.width = width;
		this.height = height;
	}

	public LifeGameEngineConfiguration(int width, int height) {
		this(width, height, DEFAULT_THREADS);
	}
}
