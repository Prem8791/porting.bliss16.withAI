/*
 * Compatibility export for vendor binaries built against older BoringSSL.
 *
 * CBS_init became header-inline in newer BoringSSL releases. Legacy vendor
 * binaries still carry a dynamic reference to the former exported symbol.
 */

#include <stddef.h>
#include <stdint.h>

typedef struct {
    const uint8_t *data;
    size_t len;
} CBS;

__attribute__((visibility("default")))
void CBS_init(CBS *cbs, const uint8_t *data, size_t len) {
    cbs->data = data;
    cbs->len = len;
}
