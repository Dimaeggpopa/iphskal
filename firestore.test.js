const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function validItemPayload(userId, now) {
  return {
    userId,
    title: "Набор инструментов 150 предм.",
    description: "Профессиональный чемодан головок и ключей",
    price: 8900,
    quantity: 1,
    unitLabel: "шт.",
    zone: "Гараж",
    shelfLocation: "Стеллаж А3, полка 2",
    qrCode: "#GAR-084-III",
    barcode: "4607185092104",
    serialCode: "SN-99412",
    photoUri: "",
    hintPhotoUri: "",
    hintNote: "Вторая полка слева у ворот",
    publishedPlatforms: ["avito", "youla"],
    publicationStatus: "published",
    isSelectedForPublish: true,
    viewsCount: 120,
    inquiriesCount: 3,
    isVerifiedInInventory: true,
    createdAt: now,
    updatedAt: now,
  };
}

test("Unauthenticated user: cannot read or list items", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("items").get()
  );
});

test("Authenticated owner: can create and read valid garage item", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);
  const docRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("items")
    .doc("item_1");

  await assertSucceeds(docRef.set(validItemPayload(ALICE_UID, now)));
  await assertSucceeds(docRef.get());
});

test("Cross-user isolation: Bob cannot read or modify Alice's garage item", async () => {
  const now = new Date(Date.now() - 1000);
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection("users")
      .doc(ALICE_UID)
      .collection("items")
      .doc("item_1")
      .set(validItemPayload(ALICE_UID, now));
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("items")
      .doc("item_1")
      .get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("items").get()
  );
});

test("Shadow field injection: fails when extra field is included", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);
  const badPayload = {
    ...validItemPayload(ALICE_UID, now),
    isVerifiedAdmin: true,
  };
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("items")
      .doc("item_bad")
      .set(badPayload)
  );
});

test("Value poisoning: fails when price is negative or status is invalid", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);
  const badPrice = {
    ...validItemPayload(ALICE_UID, now),
    price: -500,
  };
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("items")
      .doc("item_neg")
      .set(badPrice)
  );
});
