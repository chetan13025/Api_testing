//package utils;
//
//import io.qameta.allure.Allure;
//import io.restassured.filter.Filter;
//import io.restassured.filter.FilterContext;
//import io.restassured.response.Response;
//import io.restassured.specification.FilterableRequestSpecification;
//import io.restassured.specification.FilterableResponseSpecification;
//
//
//public class AllureRestAssuredFilter implements Filter {
//
//    @Override
//    public Response filter(FilterableRequestSpecification requestSpec,
//                           FilterableResponseSpecification responseSpec,
//                           FilterContext ctx) {
//
//        // Attach request details
//        Allure.addAttachment("API Request", "text/plain", 
//                requestSpec.getMethod() + " " + requestSpec.getURI() + "\n" +
//                "Headers: " + requestSpec.getHeaders() + "\n" +
//                "Body: " + (requestSpec.getBody() != null ? requestSpec.getBody() : "No Body"));
//
//        // Proceed with actual request
//        Response response = ctx.next(requestSpec, responseSpec);
//
//        // Attach response details
//        Allure.addAttachment("API Response", "application/json", response.getBody().asString());
//
//        return response;
//    }
//}
