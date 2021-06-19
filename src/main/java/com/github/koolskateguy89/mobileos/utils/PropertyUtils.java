package com.github.koolskateguy89.mobileos.utils;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.google.common.base.Strings;

public class PropertyUtils {

	private PropertyUtils() {}

	// Adapted from javafx.beans.binding.Bindings.isEmpty
	public static BooleanBinding isBlank(ObservableStringValue op) {
		if (op == null) {
			throw new NullPointerException("Operand cannot be null");
		}

		return new BooleanBinding() {
			{
				super.bind(op);
			}

			@Override
			public void dispose() {
				super.unbind(op);
			}

			@Override
			protected boolean computeValue() {
				return Strings.nullToEmpty(op.get()).isBlank();
			}

			@Override
			public ObservableList<?> getDependencies() {
				return FXCollections.singletonObservableList(op);
			}
		};
	}

}
