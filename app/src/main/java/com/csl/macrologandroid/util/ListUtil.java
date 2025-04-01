package com.csl.macrologandroid.util;

import com.csl.macrologandroid.dtos.FoodDto;
import com.csl.macrologandroid.dtos.IngredientDto;
import com.csl.macrologandroid.dtos.PortionDto;

import java.util.ArrayList;
import java.util.List;

public class ListUtil {

    public static List<String> getPortionDescList(List<PortionDto> portionList, boolean includeGrams) {
        List<String> list = new ArrayList<>();
        for (PortionDto portion : portionList) {
            String desc = portion.getDescription();
            if (desc != null && !desc.isEmpty()) {
                if (includeGrams) {
                    list.add(desc + " (" + portion.getGrams() + " gr)");
                } else {
                    list.add(desc);
                }
            }
        }
        list.add("gram");
        return list;
    }

    public static boolean isFoodInIngredientList(String foodName, List<IngredientDto> ingredients) {
        for (IngredientDto ingredient : ingredients) {
            if (foodName.equals(ingredient.getFood().getName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFoodInList(String foodName, List<FoodDto> allFood) {
        for (FoodDto food : allFood) {
            if (foodName.equals(food.getName())) {
                return true;
            }
        }
        return false;
    }

    public static PortionDto getPortionFromListByName(String portionName, List<PortionDto> allPortions) {
        for (PortionDto portion : allPortions) {
            if (portionName.contains("gr)")) {
                portionName = portionName.substring(0, portionName.indexOf(" ("));
            }
            if (portion.getDescription().equals(portionName)) {
                return portion;
            }
        }
        return null;
    }

    public static PortionDto getPortionFromListByName(String portionName, FoodDto food) {
        List<PortionDto> portions = food.getPortions();
        for (PortionDto portion : portions) {
            if (portionName.contains("gr)")) {
                portionName = portionName.substring(0, portionName.indexOf(" ("));
            }

            if (portion.getDescription().equals(portionName)) {
                return portion;
            }
        }
        return null;
    }

    public static PortionDto getPortionFromListById(Long portionId, List<PortionDto> portions) {
        for (PortionDto portion : portions) {
            if (portionId.equals(portion.getId())) {
                return portion;
            }
        }
        return null;
    }


    public static PortionDto getPortionFromListById(Long portionId, FoodDto food) {
        List<PortionDto> portions = food.getPortions();
        for (PortionDto portion : portions) {
            if (portionId.equals(portion.getId())) {
                return portion;
            }
        }
        return null;
    }
}
