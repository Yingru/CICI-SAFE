#sdx: bphJZn3RJBnNqoCZk6k9SBD8mwSb054PXbwV7HpE80E
#pa: weQ8OFpXWhIB1AMzKX2SDJcxT738VdHCcl7mFlvOD24
#bob: UIz4bXT7accigZ7KNpEyF2igwGOgXb9gne7p13i2bWA
#rpkiroot: iMrcWFMgx6DJeLtVWvBCMzwd8EDtJtZ4L0n3YYn1hi8
#alice: V1F2853Nq8V304Yb_GInYaWTgVqmBsQwC0tXWuNmmf8
#carrot: KXwvxF_rWupThUEAKwmkMTuhV8X-hqZXOAtMkWBFapc

SAFESERVER_ALICE=$1

curl  -v -X POST http://$SAFESERVER_ALICE:7777/postIdSet -H "Content-Type: application/json" -d "{ \"principal\": \"alice\", \"otherValues\": [\"alice\"] }"

curl  -v -X POST http://$SAFESERVER_ALICE:7777/postSubjectSet -H "Content-Type: application/json" -d "{ \"principal\": \"alice\", \"otherValues\": [] }"

#Alice post her access control policy, comment it out if we don't allow alice to talk to any other
curl  -v -X POST http://$SAFESERVER_ALICE:7777/postACLPolicy -H "Content-Type: application/json" -d "{ \"principal\": \"alice\", \"otherValues\": [] }"

#alice endorse a project authority
curl  -v -X POST http://$SAFESERVER_ALICE:7777/postEndorsePA -H "Content-Type: application/json" -d "{ \"principal\": \"alice\", \"otherValues\": [\"weQ8OFpXWhIB1AMzKX2SDJcxT738VdHCcl7mFlvOD24\"] }"


echo "update subject set of alice"

#Alice put the tocken for ip delegation and project membership delegation in her subject set
curl  -v -X POST http://$SAFESERVER_ALICE:7777/updateSubjectSet -H "Content-Type: application/json" -d "{ \"principal\": \"alice\", \"otherValues\": [\"fmK3VtqBFS6vhpxEZjgABOf-ATBDNdtgiOckdiOTmPg\"] }"

curl  -v -X POST http://$SAFESERVER_ALICE:7777/updateSubjectSet -H "Content-Type: application/json" -d "{ \"principal\": \"alice\", \"otherValues\": [\"f7qb7S-i7x57SClCEyl_QGujmOWTd6BeWBRQbXQxcII\"] }"
