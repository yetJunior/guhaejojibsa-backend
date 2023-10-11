/**
 * @project backend
 * @author ARA
 * @since 2023-10-11 PM 7:01
 */

package mutsa.common.domain.models.image;

import mutsa.common.domain.models.Status;

public enum ImageStatusFilter {
    ACTIVE, DELETED, ALL;

    public Status getStatus() {
        if (this == ALL) {
            return null;
        }

        return Status.valueOf(this.name());
    }
}
