package br.com.divulgaifback.common.controllers;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.PathBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class BaseController {

    protected BooleanBuilder buildOperatorPredicate(Map<String, String> params, EntityPathBase<?> qEntity) {
        BooleanBuilder builder = new BooleanBuilder();
        PathBuilder pathBuilder = new PathBuilder(qEntity.getType(), qEntity.getMetadata());

        params.forEach((key, value) -> {
            if (key.contains(".")) {
                String[] parts = key.split("\\.");
                if (parts.length == 2) {
                    String fieldName = parts[0];
                    String operator = parts[1];
                    addPredicateForField(builder, pathBuilder, fieldName, operator, value);
                }
            }
        });

        return builder;
    }

    private void addPredicateForField(BooleanBuilder builder, PathBuilder pathBuilder,
                                      String fieldName, String operator, String value) {
        switch (operator.toLowerCase()) {
            case "eq":
                builder.and(pathBuilder.get(fieldName).eq(convertValue(value)));
                break;
            case "ne":
                builder.and(pathBuilder.get(fieldName).ne(convertValue(value)));
                break;
            case "goe":
                builder.and(pathBuilder.getComparable(fieldName, Comparable.class).goe(convertValue(value)));
                break;
            case "loe":
                builder.and(pathBuilder.getComparable(fieldName, Comparable.class).loe(convertValue(value)));
                break;
            case "gt":
                builder.and(pathBuilder.getComparable(fieldName, Comparable.class).gt(convertValue(value)));
                break;
            case "lt":
                builder.and(pathBuilder.getComparable(fieldName, Comparable.class).lt(convertValue(value)));
                break;
            case "like":
                builder.and(pathBuilder.getString(fieldName).containsIgnoreCase(value));
                break;
            case "startswith":
                builder.and(pathBuilder.getString(fieldName).startsWithIgnoreCase(value));
                break;
        }
    }

    private Comparable convertValue(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e1) {
            try {
                return Double.valueOf(value);
            } catch (NumberFormatException e2) {
                try {
                    return LocalDate.parse(value);
                } catch (Exception e3) {
                    try {
                        return LocalDateTime.parse(value);
                    } catch (Exception e4) {
                        return value;
                    }
                }
            }
        }
    }
}