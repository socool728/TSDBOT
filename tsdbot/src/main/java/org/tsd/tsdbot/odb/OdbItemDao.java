package org.tsd.tsdbot.odb;

import io.dropwizard.hibernate.AbstractDAO;
import io.dropwizard.hibernate.UnitOfWork;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.tsdbot.util.OdbUtils;

import javax.persistence.Query;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OdbItemDao extends AbstractDAO<OdbItem> {

    private static final Logger log = LoggerFactory.getLogger(OdbItemDao.class);

    public OdbItemDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @UnitOfWork
    public String addItem(String item, String... tags) throws OmniDbException {
        OdbItem existingItem = isItemAlreadyStored(item);
        if (existingItem != null) {
            throw new OmniDbException("Item already exists: " + existingItem.getId());
        }

        if (ArrayUtils.isEmpty(tags)) {
            throw new OmniDbException("No tags provided");
        }

        // sanitize
        List<String> sanitizedTags = Arrays.stream(tags)
                .map(OdbUtils::sanitizeTag)
                .collect(Collectors.toList());

        OdbItem odbItem = new OdbItem();
        odbItem.setItem(item);
        odbItem.setTags(sanitizedTags);
        currentSession().persist(odbItem);
        return odbItem.getId();
    }

    @UnitOfWork
    public void deleteItem(String itemId) throws OmniDbException {
        if (StringUtils.isBlank(itemId)) {
            throw new OmniDbException("No item to delete");
        }

        OdbItem item = get(itemId);
        if (item == null) {
            throw new OmniDbException("Item with ID "+itemId+" does not exist");
        }

        currentSession().delete(item);
    }

    @UnitOfWork
    public OdbItem getRandomItem() throws OmniDbException {
        List result = currentSession()
                .createQuery("FROM OdbItem ORDER BY rand()")
                .setMaxResults(1)
                .getResultList();
        if (CollectionUtils.isEmpty(result)) {
            throw new OmniDbException("No items in Omni Database");
        }
        return (OdbItem) result.get(0);
    }

    @UnitOfWork
    public OdbItem searchForItem(List<String> tags) throws OmniDbException {
        log.info("Searching for item with tags: {}", tags);
        if (CollectionUtils.isEmpty(tags)) {
            return getRandomItem();
        }

        Map<String, String> parameters = new HashMap<>();
        StringBuilder queryString = new StringBuilder()
                .append("FROM OdbItem odbItem WHERE ");
        int i=0;
        for (String tag : tags) {
            if (i > 0) {
                queryString.append(" AND ");
            }
            String tagParam = ":tag"+i;
            queryString.append(tagParam).append(" IN elements(odbItem.tags)");
            parameters.put("tag"+i, OdbUtils.sanitizeTag(tag));
            i++;
        }

        log.info("Build ODB search query: {}", queryString.toString());
        Query query = currentSession()
                .createQuery(queryString.toString()+" ORDER BY rand()")
                .setMaxResults(1);
        for (String tag : parameters.keySet()) {
            query.setParameter(tag, parameters.get(tag));
        }

        List result = query.getResultList();
        return CollectionUtils.isEmpty(result) ? null : (OdbItem) result.get(0);
    }

    private OdbItem isItemAlreadyStored(String itemData) {
        List existingItems = currentSession()
                .createQuery("FROM OdbItem WHERE item = :item")
                .setParameter("item", itemData)
                .getResultList();
        return CollectionUtils.isEmpty(existingItems) ? null : (OdbItem) existingItems.get(0);
    }
}
