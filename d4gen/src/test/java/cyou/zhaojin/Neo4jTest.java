package cyou.zhaojin;

import org.junit.Test;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.List;

public class Neo4jTest {
    private Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", ""));
    @Test
    public void prepare() {
        try (Session session = driver.session()) {
            session.run("CALL gds.graph.project.cypher(\n" +
                    "  \"Family7\",\n" +
                    "  \"MATCH (n:Family) RETURN id(n) AS id\",\n" +
                    "  \"MATCH (n:Family)-[r:NEIGHBOR|IS_SIMILAR]-(m:Family) WITH n, m, r " +
                    "  WHERE r.identity IS NOT NULL OR r.weight IS NOT NULL RETURN id(n) AS source, id(m) AS target\")");
        }
    }

    @Test
    public void setProperty() {
        try (Session session = driver.session()){
            Result results = session.run("CALL gds.labelPropagation.stream('Family7') " +
                    "YIELD nodeId, communityId " +
                    "with collect(nodeId) as nodeIds, communityId " +
                    "return nodeIds, communityId");
            while (results.hasNext()) {
                Record next = results.next();
                List<Long> empty = new ArrayList<>();
                List<Long> nodeIds = (List<Long>) next.get("nodeIds", empty);
                Long commonnityId = next.get("communityId").asLong();
                nodeIds.forEach(n -> {
                    session.run("match (n:Family) where id(n)="+n+" set n.communityId="+commonnityId);
                });
            }
        }
    }
}
