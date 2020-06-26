

.PHONY: run update

# refetch dependencies
# overrides the lock files
# and recompile the project 
update:
	DENO_DIR=./deno_dir deno cache --reload src/index.ts

# runs the projects
# may fetch the dependencies that are not in the cache
# but will check the hashes with the lockfile
run:
	DENO_DIR=./deno_dir deno run --allow-net src/index.ts