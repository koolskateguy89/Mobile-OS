package com.github.koolskateguy89.mobileos.view.recents;

import java.util.ArrayList;
import java.util.List;

import com.github.koolskateguy89.mobileos.app.App;

import lombok.Getter;

public class RecentsController {

	@Getter // maybe should be a Stack or some sort of HashStack
	private List<App> recents = new ArrayList<>();

}
