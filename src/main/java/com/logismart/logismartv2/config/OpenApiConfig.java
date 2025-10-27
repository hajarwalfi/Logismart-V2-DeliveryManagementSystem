package com.logismart.logismartv2.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI logismartOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Local Development Server - PostgreSQL");

        Server productionServer = new Server();
        productionServer.setUrl("https://api.logismart.ma");
        productionServer.setDescription("Production Server (Future)");

        Contact contact = new Contact();
        contact.setName("SmartLogi Development Team");
        contact.setEmail("dev@logismart.ma");
        contact.setUrl("https://logismart.ma");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        
        String description = "# SmartLogi Delivery Management System (SDMS)\n\n" +
                "RESTful API for managing package deliveries across Morocco.\n\n" +
                "## Features\n" +
                "- **Client Management**: Manage sender clients and recipients\n" +
                "- **Parcel Tracking**: Full lifecycle tracking from creation to delivery\n" +
                "- **Delivery Planning**: Assign parcels to delivery personnel and zones\n" +
                "- **Priority Management**: Handle normal, urgent, and express deliveries\n" +
                "- **Status Tracking**: Monitor parcel status (CREATED, COLLECTED, IN_STOCK, IN_TRANSIT, DELIVERED)\n" +
                "- **History Tracking**: Complete audit trail for all parcel status changes\n" +
                "- **Analytics**: Statistical insights and grouping by various criteria\n" +
                "- **Multi-Product Parcels**: Associate multiple products with each parcel\n\n" +
                "## Testing Flow\n" +
                "1. Create **Zones** for delivery areas\n" +
                "2. Create **Sender Clients** (businesses/individuals who send parcels)\n" +
                "3. Create **Recipients** (people receiving parcels)\n" +
                "4. Create **Delivery Persons** and assign them to zones\n" +
                "5. Create **Products** (optional, for parcel contents)\n" +
                "6. Create **Parcels** linking sender, recipient, and products\n" +
                "7. Update parcel status and track via **Delivery History**\n" +
                "8. Use **Statistics** endpoints for analytics and reporting\n\n" +
                "## Pagination & Sorting\n" +
                "Most list endpoints support pagination with query parameters:\n" +
                "- `page`: Page number (0-indexed, default: 0)\n" +
                "- `size`: Page size (default: 20)\n" +
                "- `sort`: Sort field and direction (e.g., `createdAt,desc`)\n\n" +
                "Example: `/api/parcels?page=0&size=10&sort=createdAt,desc`\n\n" +
                "## Status Values\n" +
                "- **CREATED**: Parcel request created, awaiting collection\n" +
                "- **COLLECTED**: Parcel collected from sender\n" +
                "- **IN_STOCK**: Parcel stored in warehouse\n" +
                "- **IN_TRANSIT**: Parcel out for delivery\n" +
                "- **DELIVERED**: Parcel delivered to recipient\n\n" +
                "## Priority Levels\n" +
                "- **NORMAL**: Standard delivery\n" +
                "- **URGENT**: Priority delivery\n" +
                "- **EXPRESS**: Express/same-day delivery\n\n" +
                "## Technical Stack\n" +
                "- Spring Boot 3.3.5\n" +
                "- PostgreSQL Database\n" +
                "- Liquibase for migrations\n" +
                "- MapStruct for DTO mapping\n" +
                "- Bean Validation (JSR-380)\n" +
                "- SLF4J for logging";

        Info info = new Info()
                .title("SmartLogi Delivery Management System API")
                .version("v0.1.0")
                .description(description)
                .contact(contact)
                .license(license)
                .termsOfService("https://logismart.ma/terms");

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer, productionServer));
    }
}
