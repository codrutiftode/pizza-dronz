package uk.ac.ed.inf.api;

import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.CreditCardInformation;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Pizza;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class OrderValidator implements uk.ac.ed.inf.ilp.interfaces.OrderValidation {

    private boolean isValidCardNumber(String cardNumber) {
        return cardNumber.length() == 16;
    }

    private boolean isValidCardCVV(String cvv) {
        return cvv.length() == 3;
    }

    /**
     * Tests if the card is not already expired
     * @param expiry the expiry date string
     * @return true if the expiry date is in the future or today, false otherwise
     */
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

    /**
     * Tests if the total price in order is accurate
     * @param order the order object to test
     * @return true if the two numbers match, false otherwise
     */
    private boolean isTotalPriceCorrect(Order order) {
        int sum = 0;
        for (Pizza pizza : order.getPizzasInOrder()) sum += pizza.priceInPence();
        return sum + SystemConstants.ORDER_CHARGE_IN_PENCE == order.getPriceTotalInPence();
    }

    /**
     * Checks if all pizzas belong to at least one existing restaurant
     * @param pizzas pizzas to check
     * @param restaurantFinder to get restaurant for a given pizza
     * @return true if all pizzas exist at a restaurant, false otherwise
     */
    private boolean allPizzasDefined(Pizza[] pizzas, RestaurantFinder restaurantFinder) {
        boolean allPizzasDefined = true;
        for (Pizza pizza : pizzas) {
            Restaurant restaurant = restaurantFinder.getRestaurantForPizza(pizza);
            allPizzasDefined = allPizzasDefined & (restaurant != null);
        }
        return allPizzasDefined;
    }

    /**
     * Checks if an order contains too many pizzas
     * @param pizzas pizzas in order
     * @return true if too many, false otherwise
     */
    private boolean tooManyPizzas(Pizza[] pizzas) {
        return pizzas.length > 4;
    }

    /**
     * Checks if an order contains pizzas from multiple different restaurants
     * @param pizzas pizzas in the order
     * @param restaurantFinder to get restaurant for a given pizza
     * @return true if pizzas correspond to different restaurants, false otherwise
     */
    private boolean isMultipleRestaurants(Pizza[] pizzas, RestaurantFinder restaurantFinder) {
        String lastRestaurant = null;
        for (Pizza pizza : pizzas) {
            String restaurant = restaurantFinder.getRestaurantForPizza(pizza).name();
            if (lastRestaurant == null || restaurant.equals(lastRestaurant)) {
                lastRestaurant = restaurant;
            }
            else {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the restaurant is closed on a given order date
     * @param orderDate the order date
     * @param restaurant the restaurant
     * @return true if the restaurant is closed on that date, false otherwise
     */
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
        RestaurantFinder restaurantFinder = new RestaurantFinder(definedRestaurants);

        // Mark order as invalid if any of the fields are null
        if (orderToValidate.getPizzasInOrder() == null ||
                orderToValidate.getOrderDate() == null ||
                orderToValidate.getOrderNo() == null ||
                orderToValidate.getOrderValidationCode() == null ||
                orderToValidate.getOrderStatus() == null ||
                orderToValidate.getCreditCardInformation() == null) {
            // There is no error code for this, so leave it as undefined
            orderToValidate.setOrderValidationCode(OrderValidationCode.UNDEFINED);
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            return orderToValidate;
        }


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
        if (!isTotalPriceCorrect(orderToValidate)) {
            orderToValidate.setOrderValidationCode(OrderValidationCode.TOTAL_INCORRECT);
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            return orderToValidate;
        }

        // Test all pizzas are defined and belong to one restaurant
        if (!allPizzasDefined(orderToValidate.getPizzasInOrder(), restaurantFinder)) {
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
        if (isMultipleRestaurants(orderToValidate.getPizzasInOrder(), restaurantFinder)) {
            orderToValidate.setOrderValidationCode(OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS);
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            return orderToValidate;
        }

        // If here, then pizzas are all defined and from the same restaurant
        Restaurant targetRestaurant = restaurantFinder.getRestaurantForOrder(orderToValidate);

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
