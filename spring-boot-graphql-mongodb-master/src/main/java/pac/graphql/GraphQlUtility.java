package pac.graphql;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import pac.dataFetchers.AllUsersDataFetcher;
import pac.dataFetchers.ArticlesDataFetcher;
import pac.dataFetchers.UserDataFetcher;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

import static graphql.GraphQL.newGraphQL;

@Component
public class GraphQlUtility {

    private final AllUsersDataFetcher allUsersDataFetcher;
    private final UserDataFetcher userDataFetcher;
    private final ArticlesDataFetcher articlesDataFetcher;

    @Value("classpath:schemas.graphqls")
    private Resource schemaResource;
    private GraphQL graphQL;

    @Autowired
    public GraphQlUtility(AllUsersDataFetcher allUsersDataFetcher, UserDataFetcher userDataFetcher, ArticlesDataFetcher articlesDataFetcher) {
        this.allUsersDataFetcher = allUsersDataFetcher;
        this.userDataFetcher = userDataFetcher;
        this.articlesDataFetcher = articlesDataFetcher;
    }

    @PostConstruct
    public GraphQL createGraphQlObject() throws IOException {
        File schemas = schemaResource.getFile();
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(schemas);
        RuntimeWiring wiring = buildRuntimeWiring();
        GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring);
        return newGraphQL(schema).build();
    }

    private RuntimeWiring buildRuntimeWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type("Query", typeWiring -> typeWiring
                        .dataFetcher("users", allUsersDataFetcher)
                        .dataFetcher("user", userDataFetcher))
                .type("User", typeWiring -> typeWiring
                        .dataFetcher("articles", articlesDataFetcher)
                        .dataFetcher("friends", allUsersDataFetcher))
                .build();
    }
}

