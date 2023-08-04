package com.github.marcelomrwin.javafx;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import javafx.application.Application;

@QuarkusMain
public class QuarkusApp implements QuarkusApplication{
	public static void main(String[] args) {
        Quarkus.run(QuarkusApp.class);
    }

    @Override
    public int run(final String... args) {
        Application.launch(FxApplication.class, args);
        return 0;
    }
}
