#include <qsee_heap.h>
#include <stddef.h>
#include <stdint.h>
#include <string.h>

void* malloc(size_t size)
{
	return  qsee_malloc(size);
}

void free(void *ptr)
{
	qsee_free(ptr);
}

void* realloc(void* ptr, size_t size)
{
	return qsee_realloc(ptr, size);
}

void *zalloc(size_t size)
{
	return qsee_zalloc(size);
}

void *calloc(size_t num, size_t size)
{
	return qsee_calloc(num, size);
}
