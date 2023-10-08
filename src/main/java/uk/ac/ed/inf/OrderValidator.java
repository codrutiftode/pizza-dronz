package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.CreditCardInformation;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Pizza;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.temporal.TemporalField;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
        int expMonth = Integer.parseInt(expiryItems[0]);
        int expYear = Integer.parseInt(expiryItems[1]);

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

    private boolean allPizzasDefined(Pizza[] pizzas, Restaurant[] restaurants) {
        return true; // TODO
    }

    private boolean tooManyPizzas(Pizza[] pizzas) {
        return pizzas.length > 4;
    }

    private boolean isMultipleRestaurants(Pizza[] pizzas, Restaurant[] restaurants) {
        return true; // TODO
    }

    @Override
    public Order validateOrder(Order orderToValidate, Restaurant[] definedRestaurants) {
        CreditCardInformation cardInfo = orderToValidate.getCreditCardInformation();

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
        if (!allPizzasDefined(orderToValidate.getPizzasInOrder(), definedRestaurants)) {
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
        if (isMultipleRestaurants(orderToValidate.getPizzasInOrder(), definedRestaurants)) {
            orderToValidate.setOrderValidationCode(OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS);
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            return orderToValidate;
        }

        // No error found, pizza is valid
        orderToValidate.setOrderValidationCode(OrderValidationCode.NO_ERROR);
        orderToValidate.setOrderStatus(OrderStatus.VALID_BUT_NOT_DELIVERED);
        return orderToValidate;
    }
}
