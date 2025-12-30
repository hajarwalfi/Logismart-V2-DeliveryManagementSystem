package com.logismart.logismartv2.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
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
        contact.setName("Logismart Development Team");
        contact.setEmail("dev@logismart.ma");
        contact.setUrl("https://logismart.ma");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        String description = "# Logismart Delivery Management System (SDMS)\n\n" +
                "RESTful API for managing package deliveries across Morocco.\n\n" +
                "## 🔐 Authentication\n" +
                "This API uses **JWT Bearer tokens** for authentication.\n\n" +
                "### How to Authenticate:\n" +
                "1. **Login**: Send POST request to `/api/auth/login` with credentials\n" +
                "2. **Get Token**: Copy the JWT token from the response\n" +
                "3. **Authorize**: Click the 🔓 **Authorize** button at the top right\n" +
                "4. **Enter Token**: Paste your JWT token in the format: `Bearer <your-token>`\n" +
                "5. **Test**: Try any protected endpoint\n\n" +
                "### Test Users (username / password):\n" +
                "- **Admin**: `admin` / `admin123` - Full access including permission management\n" +
                "- **Manager**: `manager` / `manager123` - All business operations\n" +
                "- **Livreur**: `livreur` / `livreur123` - Delivery operations only\n" +
                "- **Client**: `client` / `client123` - Create and track own parcels\n\n" +
                "## Features\n" +
                "- **JWT Authentication**: Secure token-based authentication\n" +
                "- **Role-Based Access Control (RBAC)**: 4 roles with different permissions\n" +
                "- **Dynamic Permissions**: Admin can manage permissions without code changes\n" +
                "- **Client Management**: Manage sender clients and recipients\n" +
                "- **Parcel Tracking**: Full lifecycle tracking from creation to delivery\n" +
                "- **Delivery Planning**: Assign parcels to delivery personnel and zones\n" +
                "- **Priority Management**: Handle normal, urgent, and express deliveries\n" +
                "- **Status Tracking**: Monitor parcel status (CREATED, COLLECTED, IN_STOCK, IN_TRANSIT, DELIVERED)\n" +
                "- **History Tracking**: Complete audit trail for all parcel status changes\n" +
                "- **Analytics**: Statistical insights and grouping by various criteria\n" +
                "- **Multi-Product Parcels**: Associate multiple products with each parcel\n\n" +
                "## Testing Flow\n" +
                "1. **Authenticate** with one of the test users above\n" +
                "2. Create **Zones** for delivery areas\n" +
                "3. Create **Sender Clients** (businesses/individuals who send parcels)\n" +
                "4. Create **Recipients** (people receiving parcels)\n" +
                "5. Create **Delivery Persons** and assign them to zones\n" +
                "6. Create **Products** (optional, for parcel contents)\n" +
                "7. Create **Parcels** linking sender, recipient, and products\n" +
                "8. Update parcel status and track via **Delivery History**\n" +
                "9. Use **Statistics** endpoints for analytics and reporting\n\n" +
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
                "- Spring Boot 3.3.5 + Spring Security 6\n" +
                "- JWT Authentication (Bearer tokens)\n" +
                "- PostgreSQL Database\n" +
                "- Liquibase for migrations\n" +
                "- MapStruct for DTO mapping\n" +
                "- Bean Validation (JSR-380)\n" +
                "- OpenAPI 3.0 (Swagger)\n" +
                "- Docker + Docker Compose";

        Info info = new Info()
                .title("Logismart Delivery Management System API")
                .version("v0.2.0")
                .description(description)
                .contact(contact)
                .license(license)
                .termsOfService("https://logismart.ma/terms");

        // JWT Security Scheme
        SecurityScheme jwtSecurityScheme = new SecurityScheme()
                .name("JWT Authentication")
                .description("Enter JWT token in format: Bearer <token>")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER);

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("JWT Authentication");

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer, productionServer))
                .addSecurityItem(securityRequirement)
                .components(new Components()
                        .addSecuritySchemes("JWT Authentication", jwtSecurityScheme));
    }
}
