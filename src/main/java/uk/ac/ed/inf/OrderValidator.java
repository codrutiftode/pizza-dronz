package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.CreditCardInformation;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Pizza;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;

public class OrderValidator implements uk.ac.ed.inf.ilp.interfaces.OrderValidation {

    private boolean isValidCardNumber(String cardNumber) {
        return cardNumber.length() == 16;
    }

    private boolean isValidCardCVV(String cvv) {
        return cvv.length() == 3;
    }

    private boolean isValidCardExpiry(String expiry) {
        // Parse expiry date
        String[] expiryItems = expiry.split("/");
        int expMonth = Integer.parseInt(expiryItems[0].trim());
        int expYear = Integer.parseInt(expiryItems[1].trim());

        // Get current month and year
        LocalDate curDate = LocalDate.now();
        int curYear = curDate.getYear() % 100;
        int curMonth = curDate.getMonthValue();
        return expYear > curYear || (expYear == curYear && expMonth >= curMonth);
    }

    private boolean isTotalPriceCorrect(Pizza[] pizzas, int totalInPence) {
        int sum = 0;
        for (Pizza pizza : pizzas) sum += pizza.priceInPence();
        return sum == totalInPence;
    }

    private HashMap<String, Restaurant> getPizzaToRestaurantMap(Restaurant[] restaurants) {
        HashMap<String, Restaurant> pizzaToRestaurant = new HashMap<>();
        for (Restaurant restaurant : restaurants) {
            Pizza[] pizzas = restaurant.menu();
            for (Pizza pizza : pizzas) {
                pizzaToRestaurant.put(pizza.name(), restaurant);
            }
        }
        return pizzaToRestaurant;
    }

    private boolean allPizzasDefined(Pizza[] pizzas, HashMap<String, Restaurant> pizzaToRestaurantMap) {
        boolean allPizzasDefined = true;
        for (Pizza pizza : pizzas) {
            Restaurant restaurant = pizzaToRestaurantMap.get(pizza.name());
            allPizzasDefined = allPizzasDefined & (restaurant != null);
        }
        return allPizzasDefined;
    }

    private boolean tooManyPizzas(Pizza[] pizzas) {
        return pizzas.length > 4;
    }

    private boolean isMultipleRestaurants(Pizza[] pizzas, HashMap<String, Restaurant> pizzaToRestaurantMap) {
        String lastRestaurant = null;
        for (Pizza pizza : pizzas) {
            String restaurant = pizzaToRestaurantMap.get(pizza.name()).name();
            if (lastRestaurant == null || restaurant.equals(lastRestaurant)) {
                lastRestaurant = restaurant;
            }
            else {
                return true;
            }
        }
        return false;
    }

    private boolean isRestaurantClosed(LocalDate orderDate, Restaurant restaurant) {
        for (DayOfWeek dayOfWeek : restaurant.openingDays()) {
            if (orderDate.getDayOfWeek() == dayOfWeek) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Order validateOrder(Order orderToValidate, Restaurant[] definedRestaurants) {
        CreditCardInformation cardInfo = orderToValidate.getCreditCardInformation();
        HashMap<String, Restaurant> pizzaToRestaurantMap = getPizzaToRestaurantMap(definedRestaurants);

        // Test card number
        if (!isValidCardNumber(cardInfo.getCreditCardNumber())) {
            orderToValidate.setOrderValidationCode(OrderValidationCode.CARD_NUMBER_INVALID);
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            return orderToValidate;
        }

        // Test card expiry
        if (!isValidCardExpiry(cardInfo.getCreditCardExpiry())) {
            orderToValidate.setOrderValidationCode(OrderValidationCode.EXPIRY_DATE_INVALID);
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            return orderToValidate;
        }

        // Test card CVV
        if (!isValidCardCVV(cardInfo.getCvv())) {
            orderToValidate.setOrderValidationCode(OrderValidationCode.CVV_INVALID);
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            return orderToValidate;
        }

        // Test total price
        if (!isTotalPriceCorrect(orderToValidate.getPizzasInOrder(),
                orderToValidate.getPriceTotalInPence())) {
            orderToValidate.setOrderValidationCode(OrderValidationCode.TOTAL_INCORRECT);
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            return orderToValidate;
        }

        // Test all pizzas are defined and belong to one restaurant
        if (!allPizzasDefined(orderToValidate.getPizzasInOrder(), pizzaToRestaurantMap)) {
            orderToValidate.setOrderValidationCode(OrderValidationCode.PIZZA_NOT_DEFINED);
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            return orderToValidate;
        }

        // Test number of pizzas does not exceed maximum count
        if (tooManyPizzas(orderToValidate.getPizzasInOrder())) {
            orderToValidate.setOrderValidationCode(OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED);
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            return orderToValidate;
        }

        // Test if pizza was ordered from multiple restaurants
        if (isMultipleRestaurants(orderToValidate.getPizzasInOrder(), pizzaToRestaurantMap)) {
            orderToValidate.setOrderValidationCode(OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS);
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            return orderToValidate;
        }

        // If here, then pizzas are all defined and from the same restaurant
        Restaurant targetRestaurant = pizzaToRestaurantMap.get(orderToValidate.getPizzasInOrder()[0].name());

        // Check if target restaurant closed
        if (isRestaurantClosed(orderToValidate.getOrderDate(), targetRestaurant)) {
            orderToValidate.setOrderValidationCode(OrderValidationCode.RESTAURANT_CLOSED);
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            return orderToValidate;
        }

        // No error found, pizza is valid
        orderToValidate.setOrderValidationCode(OrderValidationCode.NO_ERROR);
        orderToValidate.setOrderStatus(OrderStatus.VALID_BUT_NOT_DELIVERED);
        return orderToValidate;
    }
}
