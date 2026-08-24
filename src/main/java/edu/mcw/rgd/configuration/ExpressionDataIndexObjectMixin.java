package edu.mcw.rgd.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Jackson mixin that trims fields of {@code ExpressionDataIndexObject} that are internal to the
 * Elasticsearch index out of REST responses. A mixin is used rather than an annotation on the class
 * itself because the class lives in rgdcore, where the field is still needed by the indexing pipeline.
 *
 * <p>parentTermAccIds holds the ontology ancestors used to make term queries match descendants -- an
 * indexing detail, not part of the expression record an API consumer asked for. geneSymbolWithRgdId is
 * the "Symbol-RGD:id" form the gene facet is built from, redundant next to geneSymbol and geneRgdId.
 *
 * <p>Registered on the response ObjectMapper by {@link ExpressionJsonConfig}.
 */
@JsonIgnoreProperties({"parentTermAccIds", "geneSymbolWithRgdId"})
public abstract class ExpressionDataIndexObjectMixin { }
