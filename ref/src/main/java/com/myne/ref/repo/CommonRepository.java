package com.myne.ref.repo;



import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.transform.AliasToBeanResultTransformer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.myne.ref.util.CommonConstants;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class CommonRepository {

	private final EntityManager entityManager;

	public CommonRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Transactional(readOnly = true)
	public <T> Page<T> findEntitiesWithPageable(Map<String, Object> filters, String inputQuery, Class<T> resultClass,
			Pageable pageable) {
		// Query query = buildQuery(filters, inputQuery, resultClass);
		Map<String, Object> map = buildQuerywithFilters(filters, inputQuery, resultClass, null, false);
		StringBuilder queryBuilder = (StringBuilder) map.get("queryBuilder");
		Query query = (Query) map.get("query");
		return executeQueryWithPagination(query, pageable, filters, queryBuilder);

	}

	@Transactional(readOnly = true)
	public <T> Page<T> findEntitiesWithPageableWithExtraData(Map<String, Object> filters, String inputQuery,
			Class<T> resultClass, Pageable pageable, Map<String, Object> extraData) {
		// Query query = buildQuery(filters, inputQuery, resultClass);
//		Map<String, Object> map = buildQuerywithFilters(filters, inputQuery, resultClass, null, false);
		Map<String, Object> map = buildQuerywithFiltersAndExtraData(filters, inputQuery, resultClass, null, false,
				extraData);
		StringBuilder queryBuilder = (StringBuilder) map.get("queryBuilder");
		Query query = (Query) map.get("query");
		return executeQueryWithPagination(query, pageable, filters, queryBuilder);

	}

	/*
	 * public Query buildQuery1(Map<String, Object> filters, String inputQuery,
	 * Class<?> resultClass) { log.info("CommonRepository inputQuery {}",
	 * inputQuery); String orderByClause = null; String groupByClause = null; if
	 * (inputQuery.contains(CommonConstants.ORDER_BY)) { orderByClause =
	 * extractOrderOrGroupBy(inputQuery, CommonConstants.ORDER_BY); inputQuery =
	 * extractBeforeOrderOrGroupBy(inputQuery, CommonConstants.ORDER_BY); } else if
	 * (CommonConstants.ORDER_BY.equalsIgnoreCase(CommonConstants.GROUP_BY)) {
	 * groupByClause = extractOrderOrGroupBy(inputQuery, CommonConstants.GROUP);
	 * inputQuery = extractBeforeOrderOrGroupBy(inputQuery, CommonConstants.GROUP);
	 * } StringBuilder queryBuilder = new StringBuilder(inputQuery); Map<String,
	 * Object> modifiedFilters = new LinkedHashMap<>(filters); Map<String, Object>
	 * queryParams = new LinkedHashMap<>(); queryBuilder =
	 * buildQueryWithFilters(modifiedFilters, queryBuilder, queryParams);
	 * appendOrderAndGroupByClauses(queryBuilder, orderByClause, groupByClause);
	 * log.info("CommonRepository final query with where condition {}",
	 * queryBuilder.toString()); Query query =
	 * entityManager.createQuery(queryBuilder.toString(), resultClass);
	 * 
	 * for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
	 * query.setParameter(entry.getKey(), entry.getValue()); }
	 * 
	 * return query; }
	 */
//	private <T> Page<T> executeQueryWithPagination(Query query, Pageable pageable, Map<String, Object> filters,
//			StringBuilder queryBuilder1) {
//		Long total = (long) 0;
//		if (pageable != null) {
//
//			// Apply pagination
//			query.setFirstResult((int) pageable.getOffset());
//			query.setMaxResults(pageable.getPageSize());
//
//			// Count total results for pagination
//			// Construct the count query
//			String countQueryString = "SELECT COUNT(*) " + queryBuilder1.toString()
//
//					.substring(queryBuilder1.toString().toLowerCase().indexOf(CommonConstants.FROM));
//			log.info("CommonRepository count query with where condition {}", queryBuilder1.toString());
//
//			// Create the typed query for counting
//			TypedQuery<Long> countQuery = entityManager.createQuery(countQueryString, Long.class);
//
//			Query quer = findQueryWithFilters(countQuery, filters);
//			total = (Long) quer.getSingleResult();
//			log.info("Total count {}", total);
//		}
//		return executeQueryWithPagination(query, pageable, total);
//
//	}
	private <T> Page<T> executeQueryWithPagination(Query query, Pageable pageable, Map<String, Object> filters,
			StringBuilder queryBuilder1) {
		Long total = (long) 0;
		if (pageable != null) {

			// Apply pagination
			query.setFirstResult((int) pageable.getOffset());
			query.setMaxResults(pageable.getPageSize());

			// Count total results for pagination
			// Construct the count query
			String countQueryString = null;

			countQueryString = "SELECT COUNT(*) " + queryBuilder1.toString()

					.substring(queryBuilder1.toString().toLowerCase().indexOf(CommonConstants.FROM));
			log.info("CommonRepository count query with where condition {}", queryBuilder1.toString());
			/*
			 * if(queryBuilder1.toString().toLowerCase().contains("group by")) { String
			 * countQueryString1 = " select count(*) from ( " + countQueryString +" ) as a";
			 * countQueryString =countQueryString1; }
			 */
			// Create the typed query for counting
			TypedQuery<Long> countQuery = entityManager.createQuery(countQueryString, Long.class);

			Query quer = findQueryWithFilters(countQuery, filters);
			if (queryBuilder1.toString().toLowerCase().contains("group by")) {
				List<Long> results = quer.getResultList();
				total = new Long(results.size());
			} else {
				total = (Long) quer.getSingleResult();

			}
			log.info("Total count {}", total);
		}
		return executeQueryWithPagination(query, pageable, total);

	}

	private StringBuilder buildQueryWithFilters(Map<String, Object> modifiedFilters, StringBuilder queryBuilder,
			Map<String, Object> queryParams) {

		if (!queryBuilder.toString().contains("where")) {
			queryBuilder.append(" WHERE 1=1 ");
		}

		// Map to store parameters for query
		for (Map.Entry<String, Object> entry : modifiedFilters.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			String key1 = null;
			if (key.contains(".") && key.endsWith(")")) {
				// Remove the ending character (e.g., ')')
				String trimmedKey = key.substring(0, key.length() - 1);
				// Split the key by '.'
				key1 = trimmedKey.split("\\.")[1];
			} else {
				key1 = key.contains(".") ? key.split("\\.")[1] : key;
			}
			// Modify the queryBuilder based on the filter
			if (value instanceof Object[]) {
				Object[] filterData = (Object[]) value;
				String operator = (String) filterData[0];
				if ("LIKE".equalsIgnoreCase(operator)) {
					queryBuilder.append(" AND ").append(key).append(" LIKE :").append(key1).append(" ");
					queryParams.put(key1, "%" + filterData[1] + "%");
				} else if ("BETWEEN".equalsIgnoreCase(operator)) {
					queryBuilder.append(" AND ").append(key).append(" BETWEEN :").append(key1).append("1 AND :")
							.append(key1).append("2 ");
					queryParams.put(key1 + "1", filterData[1]);
					queryParams.put(key1 + "2", filterData[2]);
				} else if ("IN".equalsIgnoreCase(operator)) {
					queryBuilder.append(" AND ").append(key).append(" IN (:").append(key1).append(") ");
					queryParams.put(key1, filterData[1]);
				} else if ("NOT IN".equalsIgnoreCase(operator)) {
					queryBuilder.append(" AND ").append(key).append(" NOT IN (:").append(key1).append(") ");
					queryParams.put(key1, filterData[1]);
				} else if ("EQUALS".equalsIgnoreCase(operator)) {
					queryBuilder.append(" AND ").append(key).append(" = :").append(key1).append(" ");
					queryParams.put(key1, filterData[1]);
				}
				else if ("GREATERTHAN".equalsIgnoreCase(operator)) {
					queryBuilder.append(" AND ").append(key).append(" > :").append(key1).append(" ");
					queryParams.put(key1, filterData[1]);
				}
				else if ("EQUALSORNULL".equalsIgnoreCase(operator)) {
					queryBuilder.append(" AND ").append(key).append(" = :").append(key1).append(" ");
					queryParams.put(key1, filterData[1]);
					queryBuilder.append(" or ").append(key).append(" is null ").append(" ");
				}

				else if ("NOT_EQUALS_OR_NULL".equalsIgnoreCase(operator)) {
					queryBuilder.append(" AND (").append(key).append(" != :").append(key1).append(" OR ").append(key)
							.append(" IS NULL) ");
					queryParams.put(key1, filterData[1]);
				}

				else if ("NOT_EQUALS".equalsIgnoreCase(operator)) {
					queryBuilder.append(" AND ").append(key).append(" != :").append(key1).append(" ");
					queryParams.put(key1, filterData[1]);
				} else if ("IS_NULL".equalsIgnoreCase(operator)) {
					queryBuilder.append(" AND ").append(key).append(" is null");
					// queryParams.put(key1, filterData[1]);
				} else if ("LESSTHAN".equalsIgnoreCase(operator)) {
					queryBuilder.append(" AND ").append(key).append(" < :").append(key1).append(" ");
					queryParams.put(key1, filterData[1]);
				} else if ("LESSTHAN_OR_EQUALS".equalsIgnoreCase(operator)) {
					queryBuilder.append(" AND ").append(key).append(" <= :").append(key1).append(" ");
					queryParams.put(key1, filterData[1]);
				} else if ("GREATERTHAN_OR_EQUALS".equalsIgnoreCase(operator)) {
					queryBuilder.append(" AND ").append(key).append(" >= :").append(key1).append(" ");
					queryParams.put(key1, filterData[1]);
				} else if ("BETWEEN_OR_START".equalsIgnoreCase(operator)) {
					queryBuilder.append(" AND ( ").append(key).append(" BETWEEN :").append(key1).append("1 AND :")
							.append(key1).append("2 ");
					queryParams.put(key1 + "1", filterData[1]);
					queryParams.put(key1 + "2", filterData[2]);
				} else if ("BETWEEN_OR_END".equalsIgnoreCase(operator)) {
					queryBuilder.append(" OR ").append(key).append(" BETWEEN :").append(key1).append("1 AND :")
							.append(key1).append("2 ").append(") ");
					queryParams.put(key1 + "1", filterData[1]);
					queryParams.put(key1 + "2", filterData[2]);
				}

			} else {
				queryBuilder.append(" AND ").append(key).append(" = :").append(key1).append(" ");
				queryParams.put(key1, value);
			}
		}
		return queryBuilder;

	}

	private void appendOrderAndGroupByClauses(StringBuilder queryBuilder, String orderByClause, String groupByClause) {

		if (orderByClause != null) {
			queryBuilder.append(" " + orderByClause);
		}
		if (groupByClause != null) {
			queryBuilder.append(" " + groupByClause);
		}

	}

	private Query findQueryWithFilters(Query query, Map<String, Object> filters) {
		if (filters != null && !filters.isEmpty()) {
			for (Map.Entry<String, Object> entry : filters.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				String key1 = null;
				if (key.contains(".") && key.endsWith(")")) {
					// Remove the ending character (e.g., ')')
					String trimmedKey = key.substring(0, key.length() - 1);
					// Split the key by '.'
					key1 = trimmedKey.split("\\.")[1];
				} else {
					key1 = key.contains(".") ? key.split("\\.")[1] : key;
				}

				if (value instanceof String && ((String) value).contains("%")) {
					// Set parameter for LIKE condition
					query.setParameter(key1, value);
				} else if (value instanceof Object[]) {
					Object[] filterData = (Object[]) value;
					String operator = (String) filterData[0];
					if (CommonConstants.LIKE.equalsIgnoreCase(operator)) {
						query.setParameter(key1, "%" + filterData[1] + "%");
					} else if (CommonConstants.BETWEEN.equalsIgnoreCase(operator)) {
						query.setParameter(key1 + "1", filterData[1]);
						query.setParameter(key1 + "2", filterData[2]);
					} else if (CommonConstants.EQUALS.equalsIgnoreCase(operator)) {
						query.setParameter(key1, filterData[1]);
					} else if ("EQUALSORNULL".equalsIgnoreCase(operator)) {
						query.setParameter(key1, filterData[1]);
						query.setParameter(key1, null);
					} else if ("GREATERTHAN".equalsIgnoreCase(operator)) {
						query.setParameter(key1, filterData[1]);
					} else if ("GREATERTHAN_OR_EQUALS".equalsIgnoreCase(operator)) {
						query.setParameter(key1, filterData[1]);
					}

					else if ("NOT_EQUALS_OR_NULL".equalsIgnoreCase(operator)) {
						query.setParameter(key1, filterData[1]);

					} else if (CommonConstants.IN.equalsIgnoreCase(operator)) {
						if (filterData[1] instanceof List<?>) {
							List<?> inList = (List<?>) filterData[1];
							log.info("Setting IN parameter for key {} with values {}", key1, inList);
							query.setParameter(key1, inList);
						} else {
							query.setParameter(key1,
									Arrays.asList(Arrays.copyOfRange(filterData, 1, filterData.length)));
						}
					} else if (CommonConstants.NOT_IN.equalsIgnoreCase(operator)) {
						if (filterData[1] instanceof List<?>) {
							List<?> inList = (List<?>) filterData[1];
							log.info("Setting NOT IN parameter for key {} with values {}", key1, inList);
							query.setParameter(key1, inList);
						} else {
							query.setParameter(key1,
									Arrays.asList(Arrays.copyOfRange(filterData, 1, filterData.length)));
						}
					} else if (CommonConstants.NOT_EQUALS.equalsIgnoreCase(operator)) {
						query.setParameter(key1, filterData[1]);
					}

				}

				else {
					// Set parameter for '=' operator
					query.setParameter(key1, value);
				}
			}
		}
		return query;
	}

	private Map<String, Object> buildQuerywithFilters(Map<String, Object> filters, String inputQuery,
			Class<?> resultClass, String SqlResultSetMapping, boolean isNativeQuery) {
		log.info("CommonRepository inputQuery {}", inputQuery);
		String orderByClause = null;
		String groupByClause = null;
		String whereSubQueryClause = null;

		if (inputQuery.contains(CommonConstants.GROUP_BY)) {
			groupByClause = extractOrderOrGroupBy(inputQuery, CommonConstants.GROUP);
			inputQuery = extractBeforeOrderOrGroupBy(inputQuery, CommonConstants.GROUP);
		} else if (inputQuery.contains(CommonConstants.ORDER_BY)) {
			orderByClause = extractOrderOrGroupBy(inputQuery, CommonConstants.ORDER);
			inputQuery = extractBeforeOrderOrGroupBy(inputQuery, CommonConstants.ORDER);
		}

		StringBuilder queryBuilder = new StringBuilder(inputQuery);
		Map<String, Object> modifiedFilters = new LinkedHashMap<>(filters);
		Map<String, Object> queryParams = new LinkedHashMap<>();
		queryBuilder = buildQueryWithFilters(modifiedFilters, queryBuilder, queryParams);

		

		appendOrderAndGroupByClauses(queryBuilder, orderByClause, groupByClause);
		log.info("CommonRepository final query with where condition {}", queryBuilder.toString());
		Query query = null;
		if (isNativeQuery) {
			query = entityManager.createNativeQuery(queryBuilder.toString());
		} else {
			query = entityManager.createQuery(queryBuilder.toString(), resultClass);

		}
		for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
			System.out.println(entry.getKey());
			System.out.println(entry.getValue());
			query.setParameter(entry.getKey(), entry.getValue());
		}
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("queryBuilder", queryBuilder);
		map.put("query", query);

		return map;
	}

	private <T> Page<T> executeQueryWithPagination(Query query, Pageable pageable, Long total) {
		@SuppressWarnings("unchecked")
		List<T> resultList = query.getResultList();
		log.info("Result list {}", resultList.size());
		return new PageImpl<>(resultList, pageable, total);
	}

	@Transactional(readOnly = true)
	public <T> List<T> findEntitiesWithFilters(Map<String, Object> filters, String inputQuery, Class<T> resultClass) {
		// Query query = buildQuery(filters, inputQuery, resultClass);
		Map<String, Object> map = buildQuerywithFilters(filters, inputQuery, resultClass, null, false);
		Query query = (Query) map.get("query");
		System.out.println(query.toString());
		@SuppressWarnings("unchecked")
		List<T> resultList = query.getResultList();
		log.info("Result list {}", resultList.size());
		return resultList;
	}

	@Transactional(readOnly = true)
	public <T> List<T> findEntitiesWithFiltersWithExtraData(Map<String, Object> filters, String inputQuery,
			Class<T> resultClass, Map<String, Object> extraData) {
		// Query query = buildQuery(filters, inputQuery, resultClass);
		Map<String, Object> map = buildQuerywithFiltersAndExtraData(filters, inputQuery, resultClass, null, false,
				extraData);
		Query query = (Query) map.get("query");
		System.out.println(query.toString());
		@SuppressWarnings("unchecked")
		List<T> resultList = query.getResultList();
		log.info("Result list {}", resultList.size());
		return resultList;
	}

	@Transactional(readOnly = true)
	public <T> List<T> findEntitiesWithFiltersWithNativeQuery(Map<String, Object> filters, String inputQuery,
			Class<T> resultClass) {
		Map<String, Object> map = buildQuerywithFilters(filters, inputQuery, null, null, true);
		Query query = (Query) map.get("query");
		log.info("Query {}", query);
		query.unwrap(org.hibernate.query.Query.class)
				.setResultTransformer(new AliasToBeanResultTransformer(resultClass));
		return query.getResultList();
	}

	@Transactional(readOnly = true)
	public <T> List<T> findEntitiesWithFiltersWithNativeQueryWithExtraData(Map<String, Object> filters,
			String inputQuery, Class<T> resultClass, Map<String, Object> extraData) {
		Map<String, Object> map = buildQuerywithFiltersAndExtraData(filters, inputQuery, null, null, true, extraData);

		Query query = (Query) map.get("query");
		log.info("Query {}", query);
		query.unwrap(org.hibernate.query.Query.class)
				.setResultTransformer(new AliasToBeanResultTransformer(resultClass));
		return query.getResultList();
	}

	@Transactional(readOnly = true)
	public <T> Page<T> findEntitiesWithFiltersWithNativeQueryWithPagination(Map<String, Object> filters,
			String inputQuery, Class<T> resultClass, Pageable pageable, Map<String, Object> extraData) {
		Map<String, Object> map = buildQuerywithFiltersAndExtraData(filters, inputQuery, null, null, true, extraData);
		Query query = (Query) map.get("query");
		log.info("Query {}", query);

		List<T> totolResultList = query.getResultList();
		query.unwrap(org.hibernate.query.Query.class)
				.setResultTransformer(new AliasToBeanResultTransformer(resultClass));

		query.setFirstResult((int) pageable.getOffset());
		query.setMaxResults(pageable.getPageSize());
		List<T> resultList = query.getResultList();
		// int totalCount =
		// entityManager.createNativeQuery(inputQuery).getResultList().size();
		return new PageImpl<>(resultList, pageable, totolResultList.size());

	}

	@Transactional(readOnly = true)
	public <T> Page<T> findEntitiesWithFiltersWithNativeQueryWithPaginationWithExtraData(Map<String, Object> filters,
			String inputQuery, Class<T> resultClass, Pageable pageable, Map<String, Object> extraData) {
		Map<String, Object> map = buildQuerywithFiltersAndExtraData(filters, inputQuery, null, null, true, extraData);
		Query query = (Query) map.get("query");
		log.info("Query {}", query);

		List<T> totolResultList = query.getResultList();
		query.unwrap(org.hibernate.query.Query.class)
				.setResultTransformer(new AliasToBeanResultTransformer(resultClass));

		query.setFirstResult((int) pageable.getOffset());
		query.setMaxResults(pageable.getPageSize());
		List<T> resultList = query.getResultList();
		// int totalCount =
		// entityManager.createNativeQuery(inputQuery).getResultList().size();
		return new PageImpl<>(resultList, pageable, totolResultList.size());

	}

	private Map<String, Object> buildQuerywithFiltersAndExtraData(Map<String, Object> filters, String inputQuery,
			Class<?> resultClass, String SqlResultSetMapping, boolean isNativeQuery, Map<String, Object> extraData) {
		log.info("CommonRepository inputQuery {}", inputQuery);
		String orderByClause = null;
		String groupByClause = null;


		if (inputQuery.contains(CommonConstants.GROUP_BY)) {
			groupByClause = extractOrderOrGroupBy(inputQuery, CommonConstants.GROUP);
			inputQuery = extractBeforeOrderOrGroupBy(inputQuery, CommonConstants.GROUP);
		} else if (inputQuery.contains(CommonConstants.ORDER_BY)) {
			orderByClause = extractOrderOrGroupBy(inputQuery, CommonConstants.ORDER);
			inputQuery = extractBeforeOrderOrGroupBy(inputQuery, CommonConstants.ORDER);
		}

		StringBuilder queryBuilder = new StringBuilder(inputQuery);
		Map<String, Object> modifiedFilters = new LinkedHashMap<>(filters);
		Map<String, Object> queryParams = new LinkedHashMap<>();
		queryBuilder = buildQueryWithFilters(modifiedFilters, queryBuilder, queryParams);


 



		appendOrderAndGroupByClauses(queryBuilder, orderByClause, groupByClause);

		log.info("CommonRepository final query with where condition {}", queryBuilder.toString());
		queryBuilder = modifyFinalQuery(queryBuilder, extraData);
		Query query = null;
		if (isNativeQuery) {
			query = entityManager.createNativeQuery(queryBuilder.toString());
		} else {
			query = entityManager.createQuery(queryBuilder.toString(), resultClass);

		}
		for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
			System.out.println(entry.getKey());
			System.out.println(entry.getValue());
			query.setParameter(entry.getKey(), entry.getValue());
		}
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("queryBuilder", queryBuilder);
		map.put("query", query);

		return map;
	}

	private StringBuilder modifyFinalQuery(StringBuilder queryBuilder, Map<String, Object> extraData) {
		// TODO Auto-generated method stub

		// gopi
		log.info("queryBuilder {} ", queryBuilder);
		log.info("extraData {}", extraData);
		String originalQuery = queryBuilder.toString();
		

		if (extraData.containsKey("getAllPendingAmountSubQuery")) {
			String whereQuery = (String) extraData.get("getAllPendingAmountSubQuery");
			originalQuery = originalQuery + " " + whereQuery;
		}

		
		if (extraData.containsKey("MAX_WORK_FLOW_HISTOY_ID_BY_INCIDENT_ID")) {
			String whereQuery = (String) extraData.get("MAX_WORK_FLOW_HISTOY_ID_BY_INCIDENT_ID");
			originalQuery = originalQuery.replace("MAX_WORK_FLOW_HISTOY_ID_BY_INCIDENT_ID", whereQuery);
		}

		log.info("Final Query After Modify {}", originalQuery);
		return new StringBuilder(originalQuery);
	}

	@Transactional(readOnly = true)
	public <T> List<T> executeNativeQueryWithMapping(String inputQuery, String mappingName) {
		log.info("Query {}", inputQuery);
		List<T> list = entityManager.createNativeQuery(inputQuery, mappingName).getResultList();
		return list;
	}

	private static String extractOrderOrGroupBy(String query, String orderOrgroup) {
		String clause = "";

		// Regular expression pattern to match "ORDER BY" or "GROUP BY" followed by any
		// characters until the end
		Pattern pattern = Pattern.compile("\\b" + orderOrgroup + "\\s+BY\\b.*", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(query);

		if (matcher.find()) {
			clause = matcher.group().trim();
		}

		return clause;
	}

	private static String extractBeforeOrderOrGroupBy(String query, String orderOrgroup) {
		String beforeClause = "";

		// Regular expression pattern to match up to but not including "ORDER BY" or
		// "GROUP BY"
		Pattern pattern = Pattern.compile("^(.*?)\\b" + orderOrgroup + "\\s+BY\\b",
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher matcher = pattern.matcher(query);

		if (matcher.find()) {
			beforeClause = matcher.group(1).trim();
		}

		return beforeClause;
	}

}
