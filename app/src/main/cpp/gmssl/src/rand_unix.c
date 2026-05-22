/*
 *  Copyright 2014-2022 The GmSSL Project. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the License); you may
 *  not use this file except in compliance with the License.
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 */



#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdint.h>
#include <unistd.h>
#include <fcntl.h>
#include <gmssl/rand.h>
#include <gmssl/error.h>


#define RAND_MAX_BUF_SIZE 256

int rand_bytes(uint8_t *buf, size_t len)
{
	if (!buf) {
		error_print();
		return -1;
	}
	if (!len || len > RAND_MAX_BUF_SIZE) {
		error_print();
		return -1;
	}

	int fd = open("/dev/urandom", O_RDONLY);
	if (fd < 0) {
		error_print();
		return -1;
	}

	size_t total = 0;
	while (total < len) {
		ssize_t n = read(fd, buf + total, len - total);
		if (n <= 0) {
			close(fd);
			error_print();
			return -1;
		}
		total += n;
	}

	close(fd);
	return 1;
}
