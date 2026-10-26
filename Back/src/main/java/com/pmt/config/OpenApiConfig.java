package com.pmt.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Description de l'API exposee dans Swagger UI. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pmtOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Project Management Tool - API")
                .version("1.0.0")
                .description("""
                        API de la plateforme collaborative PMT : projets, membres, taches et historique.

                        Toutes les routes hors /api/auth attendent l'en-tete HTTP Authorization,
                        qui porte l'identifiant retourne par /api/auth/connexion.

                        Codes de reponse transverses :
                        400 donnees invalides, 403 role insuffisant,
                        404 ressource inexistante, 409 conflit avec l'etat courant.
                        """)
                .contact(new Contact().name("Code Solutions")));
    }
}
