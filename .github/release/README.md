# App update policy

The release workflow generates `kbbi-update-policy.json` from `app/version.properties`. There is no separate policy
file to synchronize manually.

For a production release:

1. Update the version components and `VERSION_CODE` in `app/version.properties`.
2. Set `FORCE_UPDATE=true` in the same file only when every older app version must update before continuing. Use
   `FORCE_UPDATE=false` for a normal optional release.
3. Commit the changes and create a three-part version tag, such as `5.25.0` or `v5.25.0`.
4. Push the tag.

The release workflow derives `releaseVersion` and `forceUpdate` from `app/version.properties`, checks that the tag
has the same version, and uploads the generated `kbbi-update-policy.json` beside the production APK. A mismatched
tag or a `FORCE_UPDATE` value other than `true` or `false` stops the release.

## App behavior

- A newer major version is always a required update, regardless of `forceUpdate`.
- `forceUpdate: true` makes a newer release required even when its major version is unchanged.
- Required updates block app use and are cached so they remain enforced while offline.
- Automatic network checks run at most once every 24 hours. Failed checks can retry after one hour.
- A manual check from Settings bypasses the automatic-check interval.

Ship the first app version containing this capability as an optional update before relying on required updates,
because older app versions cannot read the policy.
